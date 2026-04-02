package project.util.imgUpload;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface ImgService {

    String uploadFile(MultipartFile file) throws IOException;

    List<String> storeFiles(List<MultipartFile> files) throws IOException;

    void deleteFile(String key);

    void deleteFiles(List<String> keys);

    void deleteFilesByUrls(List<String> urls);

    void deleteByUrl(String url);
}
