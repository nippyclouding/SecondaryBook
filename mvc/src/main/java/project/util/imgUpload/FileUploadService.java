package project.util.imgUpload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Primary
@Slf4j
public class FileUploadService implements ImgService {

    @Value("${file.dir}")
    private String fileDir;

    // ========================================
    // 파일 업로드 보안 설정 (S3Service와 동일)
    // ========================================

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] GIF_SIGNATURE = {0x47, 0x49, 0x46, 0x38};
    private static final byte[] WEBP_SIGNATURE = {0x52, 0x49, 0x46, 0x46};

    /** 업로드 URL 접두사 (리소스 핸들러와 매칭) */
    private static final String URL_PREFIX = "/upload/";

    /** 파일 저장 하위 경로 */
    private static final String IMAGE_DIR = "images";

    // ========================================
    // 파일 검증 메서드 (S3Service와 동일)
    // ========================================

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IOException("파일명이 없습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("파일 크기 초과: {}bytes (최대 {}bytes)", file.getSize(), MAX_FILE_SIZE);
            throw new IOException("파일 크기가 5MB를 초과합니다.");
        }

        String extension = extractExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("허용되지 않는 확장자: {}", extension);
            throw new IOException("허용되지 않는 파일 형식입니다. (허용: jpg, jpeg, png, gif, webp)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("허용되지 않는 MIME 타입: {}", contentType);
            throw new IOException("허용되지 않는 파일 형식입니다.");
        }

        byte[] fileBytes = file.getBytes();
        if (!isValidImageSignature(fileBytes, extension)) {
            log.warn("파일 시그니처 불일치: 확장자={}, 실제 시그니처 불일치", extension);
            throw new IOException("파일 내용이 이미지 형식과 일치하지 않습니다.");
        }

        log.debug("파일 검증 통과: name={}, size={}, type={}", originalFilename, file.getSize(), contentType);
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

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
                return startsWith(fileBytes, WEBP_SIGNATURE) &&
                       fileBytes.length >= 12 &&
                       fileBytes[8] == 'W' && fileBytes[9] == 'E' &&
                       fileBytes[10] == 'B' && fileBytes[11] == 'P';
            default:
                return false;
        }
    }

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
    // ImgService 구현
    // ========================================

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) originalFilename = "file";
        String ext = extractExtension(originalFilename).toLowerCase();

        String filename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        String key = IMAGE_DIR + "/" + filename;

        // 디렉토리 생성
        File dir = new File(fileDir + "/" + IMAGE_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            log.info("업로드 디렉토리 생성: {} (성공: {})", dir.getAbsolutePath(), created);
        }

        // 로컬 디스크에 저장
        File dest = new File(fileDir + "/" + key);
        file.transferTo(dest);

        log.info("로컬 파일 업로드 성공: key={}, size={}", key, file.getSize());

        // /upload/images/{UUID}.jpg 형태로 반환
        return URL_PREFIX + key;
    }

    @Override
    public List<String> storeFiles(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                urls.add(uploadFile(file));
            }
        }
        return urls;
    }

    @Override
    public void deleteFile(String key) {
        if (key == null || key.trim().isEmpty()) return;

        File file = new File(fileDir + "/" + key);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                log.info("로컬 파일 삭제 성공: key={}", key);
            } else {
                log.warn("로컬 파일 삭제 실패: key={}", key);
            }
        } else {
            log.debug("삭제할 파일이 존재하지 않음: key={}", key);
        }
    }

    @Override
    public void deleteFiles(List<String> keys) {
        for (String key : keys) {
            deleteFile(key);
        }
    }

    @Override
    public void deleteFilesByUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        for (String url : urls) {
            deleteByUrl(url);
        }
    }

    @Override
    public void deleteByUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            log.debug("Empty URL provided to deleteByUrl, skipping");
            return;
        }

        // S3 URL인 경우 스킵 (마이그레이션 전 기존 데이터)
        if (url.startsWith("http://") || url.startsWith("https://")) {
            log.debug("S3 URL은 로컬 모드에서 삭제 불가, 스킵: {}", url);
            return;
        }

        // /upload/images/{filename} 형태 검증
        if (!url.startsWith(URL_PREFIX)) {
            log.warn("유효하지 않은 이미지 경로: {}", url);
            return;
        }

        String key = url.substring(URL_PREFIX.length()); // images/{filename}

        // path traversal 방지
        if (key.contains("..")) {
            log.warn("Path traversal 시도 차단: {}", url);
            return;
        }

        // prefix 검증
        if (!key.startsWith(IMAGE_DIR + "/")) {
            log.warn("허용되지 않은 경로 prefix: {}", key);
            return;
        }

        log.info("Deleting local file - URL: {}, Key: {}", url, key);
        deleteFile(key);
    }
}
