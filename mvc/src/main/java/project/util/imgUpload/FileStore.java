package project.util.imgUpload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir;

    // 허용된 이미지 확장자 목록
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );

    // 허용된 이미지 MIME 타입 목록
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    // 파라미터로 받은 파일 이름을 추가
    public String getFullPath(String fileName) {
        return fileDir + "/" + fileName;
    }

    // 멀티파트 파일들을 서버에 저장될 파일 리스트로 리턴
    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile)); // uploadFIle 객체를 리스트에 추가
            }
        }
        return storeFileResult;
    }

    // 멀티파트 파일 -> 서버에 저장될 이름 변경
    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {

        if (multipartFile.isEmpty()) {
            log.warn("빈 파일이 전달됨");
            return null;
        }

        // 디렉토리 생성 (없으면 자동 생성)
        File dir = new File(fileDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            log.info("파일 저장 디렉토리 생성: {} (성공: {})", dir.getAbsolutePath(), created);
        }

        String orgFileName = multipartFile.getOriginalFilename();
        String storeFileName = createStoreFileName(orgFileName); // 사용자가 업로드한 파일 이름 -> 서버에 저장될 이름
        String fullPath = getFullPath(storeFileName);

        log.info("파일 저장 시도: 원본={}, 저장명={}, 경로={}, 크기={}바이트",
                 orgFileName, storeFileName, fullPath, multipartFile.getSize());

        multipartFile.transferTo(new File(fullPath)); // 서버에 저장될 파일 이름으로 실제 경로에 저장

        log.info("파일 저장 완료: {}", fullPath);
        return new UploadFile(orgFileName, storeFileName);
    }

    private String createStoreFileName(String orgFileName) {
        String ext = extractExt(orgFileName); // png, jpg 등 확장자
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String extractExt(String orgFileName) {
        int pos = orgFileName.lastIndexOf(".");
        return orgFileName.substring(pos + 1);
    }

    /**
     * 이미지 파일 여부 검증
     * @param file MultipartFile
     * @return 이미지 파일이면 true
     */
    public boolean isImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 1. Content-Type 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("허용되지 않은 Content-Type: {}", contentType);
            return false;
        }

        // 2. 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            log.warn("파일명에 확장자가 없음: {}", originalFilename);
            return false;
        }

        String ext = extractExt(originalFilename).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
            log.warn("허용되지 않은 확장자: {}", ext);
            return false;
        }

        return true;
    }

    /**
     * 이미지 파일 저장 (이미지 검증 포함)
     * @param multipartFile 업로드할 파일
     * @return 저장된 파일 정보
     * @throws IOException 파일 저장 실패 시
     * @throws IllegalArgumentException 이미지 파일이 아닌 경우
     */
    public UploadFile storeImageFile(MultipartFile multipartFile) throws IOException {
        if (!isImageFile(multipartFile)) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다. (jpg, jpeg, png, gif, webp, bmp)");
        }
        return storeFile(multipartFile);
    }
}
