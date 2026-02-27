package project.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-CBC 암호화/복호화 유틸리티.
 *
 * <p>저장 형식: {@code Base64(IV) + ":" + Base64(암호문)}</p>
 * <p>키는 application.properties의 {@code aes.secret-key}에서 주입 (32바이트 Base64 인코딩).</p>
 *
 * <p>계좌번호(20자) 기준 암호문 길이: IV 24자 + ':' 1자 + 암호문 44자 = 최대 69자</p>
 */
@Component
public class AesEncryptionUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    private final SecretKeySpec secretKey;

    public AesEncryptionUtil(@Value("${aes.secret-key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "AES-256 키는 32바이트여야 합니다. 현재 길이: " + keyBytes.length);
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 평문을 AES-256-CBC로 암호화한다.
     *
     * @param plaintext 암호화할 원본 문자열
     * @return "Base64(IV):Base64(암호문)" 형식의 문자열
     */
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(iv)
                    + ":"
                    + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("계좌번호 암호화 실패", e);
        }
    }

    /**
     * "Base64(IV):Base64(암호문)" 형식의 문자열을 복호화한다.
     *
     * @param ciphertext encrypt() 가 반환한 형식의 문자열
     * @return 복호화된 원본 문자열
     */
    public String decrypt(String ciphertext) {
        try {
            String[] parts = ciphertext.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("잘못된 암호화 형식: ':' 구분자 없음");
            }
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("계좌번호 복호화 실패", e);
        }
    }
}
