package project.util.imgUpload;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 이미지 업로드/삭제 공통 인터페이스
 * 구현체: FileUploadService (로컬), S3Service (AWS S3)
 * @Primary가 붙은 구현체가 기본으로 주입됨
 */
public interface ImgService {

    /** 단일 파일 업로드 → 접근 가능한 경로/URL 반환 */
    String uploadFile(MultipartFile file) throws IOException;

    /** 복수 파일 업로드 → 경로/URL 리스트 반환 */
    List<String> storeFiles(List<MultipartFile> files) throws IOException;

    /** key(상대 경로)로 단일 파일 삭제 */
    void deleteFile(String key);

    /** key 리스트로 복수 파일 삭제 */
    void deleteFiles(List<String> keys);

    /** 전체 URL/경로로 복수 파일 삭제 */
    void deleteFilesByUrls(List<String> urls);

    /** 전체 URL/경로로 단일 파일 삭제 (보안 검증 포함) */
    void deleteByUrl(String url);
}
