package project.util.imgUpload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadFile {
    private String uploadFileName; // 사용자가 업로드한 파일명
    private String storeFileName; // 서버 내부에서 관리하는 파일명
}
