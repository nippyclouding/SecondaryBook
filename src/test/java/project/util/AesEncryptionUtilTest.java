package project.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AesEncryptionUtilTest {

    // 테스트용 32바이트 키 (Base64 인코딩)
    private static final String TEST_KEY = "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=";

    private final AesEncryptionUtil util = new AesEncryptionUtil(TEST_KEY);

    @Test
    @DisplayName("암호화 → 복호화 라운드트립 - 원본 일치")
    void encryptDecryptRoundTrip() {
        String original = "123456789012";

        String encrypted = util.encrypt(original);
        String decrypted = util.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    @DisplayName("같은 평문이라도 매번 다른 암호문 생성 (랜덤 IV)")
    void encryptProducesDifferentCiphertextEachTime() {
        String original = "123456789012";

        String enc1 = util.encrypt(original);
        String enc2 = util.encrypt(original);

        assertThat(enc1).isNotEqualTo(enc2);
    }

    @Test
    @DisplayName("최대 길이 계좌번호(20자리) 암호화/복호화")
    void encryptDecryptMaxLength() {
        String original = "12345678901234567890";

        String decrypted = util.decrypt(util.encrypt(original));

        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    @DisplayName("잘못된 형식(구분자 없음)은 RuntimeException 발생")
    void decryptInvalidFormatThrows() {
        assertThatThrownBy(() -> util.decrypt("잘못된암호문"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("키가 32바이트가 아니면 IllegalArgumentException 발생")
    void invalidKeySizeThrows() {
        String shortKey = "dG9vU2hvcnRLZXk="; // 10바이트 → 유효하지 않음

        assertThatThrownBy(() -> new AesEncryptionUtil(shortKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32바이트");
    }
}
