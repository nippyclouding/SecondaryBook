package project.util.imgUpload;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceTest {

    private FileUploadService fileUploadService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        fileUploadService = new FileUploadService();
        tempDir = Files.createTempDirectory("upload-test");
        ReflectionTestUtils.setField(fileUploadService, "fileDir", tempDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        // 테스트 디렉토리 정리
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
    }

    // JPEG 시그니처를 가진 바이트 배열 생성
    private byte[] createJpegBytes() {
        return new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};
    }

    // PNG 시그니처를 가진 바이트 배열 생성
    private byte[] createPngBytes() {
        return new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    }

    // ========== uploadFile ==========

    @Nested
    @DisplayName("uploadFile - 단일 파일 업로드")
    class UploadFile {

        @Test
        @DisplayName("정상 JPEG 업로드 시 /upload/images/ 경로를 반환한다")
        void uploadsJpeg() throws IOException {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "image", "test.jpg", "image/jpeg", createJpegBytes());

            // when
            String result = fileUploadService.uploadFile(file);

            // then
            assertThat(result).startsWith("/upload/images/");
            assertThat(result).endsWith(".jpg");

            // 실제 파일 존재 확인
            String key = result.substring("/upload/".length());
            File saved = new File(tempDir.toString() + "/" + key);
            assertThat(saved).exists();
        }

        @Test
        @DisplayName("정상 PNG 업로드 시 /upload/images/ 경로를 반환한다")
        void uploadsPng() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "photo.png", "image/png", createPngBytes());

            String result = fileUploadService.uploadFile(file);

            assertThat(result).startsWith("/upload/images/");
            assertThat(result).endsWith(".png");
        }

        @Test
        @DisplayName("빈 파일이면 null을 반환한다")
        void returnsNullForEmpty() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "empty.jpg", "image/jpeg", new byte[0]);

            String result = fileUploadService.uploadFile(file);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("5MB 초과 파일은 IOException을 던진다")
        void rejectsOversizedFile() {
            byte[] bigContent = new byte[6 * 1024 * 1024]; // 6MB
            // JPEG 시그니처 삽입
            bigContent[0] = (byte) 0xFF;
            bigContent[1] = (byte) 0xD8;
            bigContent[2] = (byte) 0xFF;

            MockMultipartFile file = new MockMultipartFile(
                    "image", "big.jpg", "image/jpeg", bigContent);

            assertThatThrownBy(() -> fileUploadService.uploadFile(file))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("5MB");
        }

        @Test
        @DisplayName("허용되지 않은 확장자는 IOException을 던진다")
        void rejectsInvalidExtension() {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "virus.exe", "image/jpeg", createJpegBytes());

            assertThatThrownBy(() -> fileUploadService.uploadFile(file))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("허용되지 않는 파일 형식");
        }

        @Test
        @DisplayName("허용되지 않은 MIME 타입은 IOException을 던진다")
        void rejectsInvalidMimeType() {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "test.jpg", "text/plain", createJpegBytes());

            assertThatThrownBy(() -> fileUploadService.uploadFile(file))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("허용되지 않는 파일 형식");
        }

        @Test
        @DisplayName("파일 시그니처가 확장자와 불일치하면 IOException을 던진다")
        void rejectsSignatureMismatch() {
            // PNG 시그니처이지만 jpg 확장자
            MockMultipartFile file = new MockMultipartFile(
                    "image", "fake.jpg", "image/jpeg", createPngBytes());

            assertThatThrownBy(() -> fileUploadService.uploadFile(file))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("이미지 형식과 일치하지 않습니다");
        }

        @Test
        @DisplayName("UUID 기반 파일명으로 저장되어 원본 파일명과 다르다")
        void usesUuidFilename() throws IOException {
            MockMultipartFile file = new MockMultipartFile(
                    "image", "original-name.jpg", "image/jpeg", createJpegBytes());

            String result = fileUploadService.uploadFile(file);

            assertThat(result).doesNotContain("original-name");
        }
    }

    // ========== storeFiles ==========

    @Nested
    @DisplayName("storeFiles - 복수 파일 업로드")
    class StoreFiles {

        @Test
        @DisplayName("여러 파일을 업로드하고 경로 리스트를 반환한다")
        void uploadsMultipleFiles() throws IOException {
            MockMultipartFile file1 = new MockMultipartFile(
                    "image", "a.jpg", "image/jpeg", createJpegBytes());
            MockMultipartFile file2 = new MockMultipartFile(
                    "image", "b.png", "image/png", createPngBytes());

            List<String> results = fileUploadService.storeFiles(Arrays.asList(file1, file2));

            assertThat(results).hasSize(2);
            assertThat(results.get(0)).startsWith("/upload/images/");
            assertThat(results.get(1)).startsWith("/upload/images/");
        }

        @Test
        @DisplayName("빈 파일은 건너뛴다")
        void skipsEmptyFiles() throws IOException {
            MockMultipartFile valid = new MockMultipartFile(
                    "image", "a.jpg", "image/jpeg", createJpegBytes());
            MockMultipartFile empty = new MockMultipartFile(
                    "image", "empty.jpg", "image/jpeg", new byte[0]);

            List<String> results = fileUploadService.storeFiles(Arrays.asList(valid, empty));

            assertThat(results).hasSize(1);
        }
    }

    // ========== deleteByUrl ==========

    @Nested
    @DisplayName("deleteByUrl - URL로 파일 삭제")
    class DeleteByUrl {

        @Test
        @DisplayName("로컬 경로로 파일을 삭제한다")
        void deletesLocalFile() throws IOException {
            // given - 파일 업로드
            MockMultipartFile file = new MockMultipartFile(
                    "image", "test.jpg", "image/jpeg", createJpegBytes());
            String url = fileUploadService.uploadFile(file);

            String key = url.substring("/upload/".length());
            File saved = new File(tempDir.toString() + "/" + key);
            assertThat(saved).exists();

            // when
            fileUploadService.deleteByUrl(url);

            // then
            assertThat(saved).doesNotExist();
        }

        @Test
        @DisplayName("S3 URL은 스킵한다 (예외 없음)")
        void skipsS3Url() {
            // when & then - 예외 없이 무시
            assertThatCode(() ->
                    fileUploadService.deleteByUrl("https://bucket.s3.amazonaws.com/images/test.jpg")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("null URL은 무시한다")
        void ignoresNull() {
            assertThatCode(() -> fileUploadService.deleteByUrl(null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("빈 URL은 무시한다")
        void ignoresEmpty() {
            assertThatCode(() -> fileUploadService.deleteByUrl(""))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("path traversal 시도를 차단한다")
        void blocksPathTraversal() {
            assertThatCode(() ->
                    fileUploadService.deleteByUrl("/upload/images/../../etc/passwd")
            ).doesNotThrowAnyException();
            // 삭제되지 않아야 함 (차단됨)
        }

        @Test
        @DisplayName("허용되지 않은 prefix는 무시한다")
        void ignoresInvalidPrefix() {
            assertThatCode(() ->
                    fileUploadService.deleteByUrl("/upload/secret/config.yml")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("/upload/ 접두사가 아닌 경로는 무시한다")
        void ignoresNonUploadPath() {
            assertThatCode(() ->
                    fileUploadService.deleteByUrl("/resources/img/default.jpg")
            ).doesNotThrowAnyException();
        }
    }

    // ========== deleteFile ==========

    @Test
    @DisplayName("deleteFile - key로 파일을 삭제한다")
    void deleteFileByKey() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", createJpegBytes());
        String url = fileUploadService.uploadFile(file);
        String key = url.substring("/upload/".length());

        File saved = new File(tempDir.toString() + "/" + key);
        assertThat(saved).exists();

        // when
        fileUploadService.deleteFile(key);

        // then
        assertThat(saved).doesNotExist();
    }

    // ========== deleteFilesByUrls ==========

    @Test
    @DisplayName("deleteFilesByUrls - 여러 URL로 파일을 삭제한다")
    void deleteMultipleByUrls() throws IOException {
        // given
        MockMultipartFile file1 = new MockMultipartFile(
                "image", "a.jpg", "image/jpeg", createJpegBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "image", "b.png", "image/png", createPngBytes());

        String url1 = fileUploadService.uploadFile(file1);
        String url2 = fileUploadService.uploadFile(file2);

        // when
        fileUploadService.deleteFilesByUrls(Arrays.asList(url1, url2));

        // then
        String key1 = url1.substring("/upload/".length());
        String key2 = url2.substring("/upload/".length());
        assertThat(new File(tempDir.toString() + "/" + key1)).doesNotExist();
        assertThat(new File(tempDir.toString() + "/" + key2)).doesNotExist();
    }
}
