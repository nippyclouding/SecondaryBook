package project.util.imgUpload;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import project.util.exception.InvalidRequestException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service implements ImgService {

    private final S3Client s3Client;

    @Value("${AWS_S3_BUCKET}")
    private String bucketName;

    @Value("${AWS_S3_REGION}")
    private String region;

    @Value("${AWS_CLOUDFRONT_DOMAIN:}")
    private String cloudFrontDomain;

    // ========================================
    // 파일 업로드 보안 설정
    // ========================================

    /** 허용된 이미지 확장자 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    /** 허용된 MIME 타입 */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /** 최대 파일 크기 (5MB) */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /** 이미지 파일 시그니처 (Magic Bytes) */
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] GIF_SIGNATURE = {0x47, 0x49, 0x46, 0x38};
    private static final byte[] WEBP_SIGNATURE = {0x52, 0x49, 0x46, 0x46}; // RIFF

    // ========================================
    // 파일 검증 메서드
    // ========================================

    /**
     * 파일 업로드 전 보안 검증
     * @param file 업로드할 파일
     * @throws IOException 검증 실패 시
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return; // 빈 파일은 uploadFile에서 null 반환
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IOException("파일명이 없습니다.");
        }

        // 1. 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("파일 크기 초과: {}bytes (최대 {}bytes)", file.getSize(), MAX_FILE_SIZE);
            throw new IOException("파일 크기가 5MB를 초과합니다.");
        }

        // 2. 확장자 검증
        String extension = extractExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("허용되지 않는 확장자: {}", extension);
            throw new IOException("허용되지 않는 파일 형식입니다. (허용: jpg, jpeg, png, gif, webp)");
        }

        // 3. MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("허용되지 않는 MIME 타입: {}", contentType);
            throw new IOException("허용되지 않는 파일 형식입니다.");
        }

        // 4. 파일 시그니처(Magic Bytes) 검증 - 실제 파일 내용 확인
        byte[] fileBytes = file.getBytes();
        if (!isValidImageSignature(fileBytes, extension)) {
            log.warn("파일 시그니처 불일치: 확장자={}, 실제 시그니처 불일치", extension);
            throw new IOException("파일 내용이 이미지 형식과 일치하지 않습니다.");
        }

        log.debug("파일 검증 통과: name={}, size={}, type={}", originalFilename, file.getSize(), contentType);
    }

    /**
     * 파일 확장자 추출
     */
    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    /**
     * 파일 시그니처(Magic Bytes) 검증
     * 파일의 첫 몇 바이트를 확인하여 실제 이미지인지 검증
     */
    private boolean isValidImageSignature(byte[] fileBytes, String extension) {
        if (fileBytes == null || fileBytes.length < 4) {
            return false;
        }

        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return startsWith(fileBytes, JPEG_SIGNATURE);
            case "png":
                return startsWith(fileBytes, PNG_SIGNATURE);
            case "gif":
                return startsWith(fileBytes, GIF_SIGNATURE);
            case "webp":
                // WEBP: RIFF....WEBP 형식
                return startsWith(fileBytes, WEBP_SIGNATURE) &&
                       fileBytes.length >= 12 &&
                       fileBytes[8] == 'W' && fileBytes[9] == 'E' &&
                       fileBytes[10] == 'B' && fileBytes[11] == 'P';
            default:
                return false;
        }
    }

    /**
     * 바이트 배열이 특정 시그니처로 시작하는지 확인
     */
    private boolean startsWith(byte[] data, byte[] signature) {
        if (data.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    // ========================================
    // 파일 업로드 메서드
    // ========================================

    /**
     * 단일 파일 업로드 (검증 포함)
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        // 파일 검증
        validateFile(file);

        // 파일명 및 확장자 처리
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) originalFilename = "file";
        String ext = extractExtension(originalFilename).toLowerCase();

        // S3에 저장될 키 생성 (UUID 기반)
        String key = "images/" + UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

        // S3 업로드 요청
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        log.info("S3 업로드 성공: key={}, size={}", key, file.getSize());

        // CloudFront URL 반환 (설정되어 있으면), 아니면 S3 URL
        if (cloudFrontDomain != null && !cloudFrontDomain.isEmpty()) {
            return String.format("https://%s/%s", cloudFrontDomain, key);
        }
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }

    // 다중 파일 업로드
    public List<String> storeFiles(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                urls.add(uploadFile(file));
            }
        }
        return urls;
    }

    // 단일 파일 삭제
    public void deleteFile(String key) {
        s3Client.deleteObject(builder -> builder.bucket(bucketName).key(key).build());
    }

    // 다중 파일 삭제
    public void deleteFiles(List<String> keys) {
        for (String key : keys) {
            deleteFile(key);
        }
    }

    // 만약 imgUrls 전체 URL이 있다면 key만 추출
    public void deleteFilesByUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;

        for (String url : urls) {
            deleteByUrl(url);
        }
    }

    /**
     * URL을 받아서 안전하게 S3 파일을 삭제합니다.
     *
     * 보안 정책:
     * 1. 우리 버킷(secondarybooksimages)의 URL만 삭제 허용
     * 2. 올바른 region(ap-northeast-2) 확인
     * 3. key prefix는 images/로 시작해야 함 (업로드 규칙과 일치)
     *
     * @param url 삭제할 S3 파일의 전체 URL
     * @throws IllegalArgumentException URL 파싱 실패 또는 보안 정책 위반 시
     */
    public void deleteByUrl(String url) {
        // null/blank 체크
        if (url == null || url.trim().isEmpty()) {
            log.debug("Empty URL provided to deleteByUrl, skipping");
            return;
        }

        try {
            // URI로 안전하게 파싱 (query string 자동 분리)
            URI uri = new URI(url.trim());
            String host = uri.getHost();
            String path = uri.getPath();

            // 보안 검증 1: host가 우리 버킷 또는 CloudFront인지 확인
            String expectedS3Host = String.format("%s.s3.%s.amazonaws.com", bucketName, region);
            boolean isS3Url = host != null && host.equals(expectedS3Host);
            boolean isCloudFrontUrl = host != null && cloudFrontDomain != null && host.equals(cloudFrontDomain);

            if (!isS3Url && !isCloudFrontUrl) {
                log.warn("Security violation: Attempted to delete file from unauthorized source. " +
                        "URL={}, expected_s3_host={}, cloudfront_domain={}, actual_host={}",
                        url, expectedS3Host, cloudFrontDomain, host);
                throw new InvalidRequestException(
                    "Only files from bucket '" + bucketName + "' or CloudFront can be deleted"
                );
            }

            // 보안 검증 2: path에서 key 추출 (leading slash 제거)
            if (path == null || path.isEmpty() || path.equals("/")) {
                log.warn("Invalid path in URL: {}", url);
                throw new InvalidRequestException("URL does not contain a valid file path");
            }

            String key = path.startsWith("/") ? path.substring(1) : path;

            // 보안 검증 3: key가 허용된 prefix로 시작하는지 확인
            // 현재 업로드는 images/ prefix를 사용하므로, 삭제도 동일한 prefix만 허용
            if (!key.startsWith("images/")) {
                log.warn("Security violation: Attempted to delete file outside allowed prefix. " +
                        "URL={}, key={}, allowed_prefix=images/", url, key);
                throw new InvalidRequestException(
                    "Only files with prefix 'images/' can be deleted. Provided key: " + key
                );
            }

            // 보안 검증 4: path traversal 시도 차단
            if (key.contains("..")) {
                log.warn("Security violation: Path traversal attempt detected. URL={}, key={}", url, key);
                throw new InvalidRequestException("Path traversal is not allowed in key: " + key);
            }

            // 모든 검증 통과 - 삭제 실행
            log.info("Deleting S3 file - URL: {}, Key: {}", url, key);
            deleteFile(key);
            log.info("Successfully deleted S3 file - Key: {}", key);

        } catch (URISyntaxException e) {
            log.error("Failed to parse URL: {}. Error: {}", url, e.getMessage());
            throw new InvalidRequestException("Invalid URL format: " + url, e);
        } catch (Exception e) {
            log.error("Failed to delete S3 file. URL: {}, Error: {}", url, e.getMessage());
            throw new RuntimeException("Failed to delete S3 file: " + url, e);
        }
    }
}