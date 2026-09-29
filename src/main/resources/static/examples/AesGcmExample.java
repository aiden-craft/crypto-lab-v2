import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** LAB 05: 새 nonce를 사용하는 AES-GCM 암복호화 예제. */
public final class AesGcmExample {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int NONCE_BYTES = 12;
    private static final int TAG_BITS = 128;

    public record Encrypted(String nonce, String ciphertext) {}

    public static Encrypted encrypt(String plainText, SecretKey key) throws Exception {
        byte[] nonce = new byte[NONCE_BYTES];
        RANDOM.nextBytes(nonce); // 같은 키로 nonce를 재사용하면 안 됩니다.

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return new Encrypted(Base64.getEncoder().encodeToString(nonce),
                Base64.getEncoder().encodeToString(encrypted));
    }

    public static String decrypt(Encrypted value, SecretKey key) throws Exception {
        byte[] nonce = Base64.getDecoder().decode(value.nonce());
        byte[] encrypted = Base64.getDecoder().decode(value.ciphertext());
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, nonce));
        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        // 암호문/태그가 변조되면 doFinal에서 인증 오류가 납니다.
    }

    public static void main(String[] args) throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        SecretKey key = generator.generateKey();
        Encrypted first = encrypt("Hello AES", key);
        Encrypted second = encrypt("Hello AES", key);

        System.out.println("서로 다른 결과: " + !first.equals(second));
        System.out.println("복호화: " + decrypt(first, key));
        // 현재 실습 화면은 AES-CBC 비교용입니다. 이 파일은 GCM 권장 예제입니다.
    }
}
