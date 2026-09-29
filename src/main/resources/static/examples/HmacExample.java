import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** LAB 07: 공유 비밀키로 HMAC-SHA256을 생성하고 검증하는 예제. */
public final class HmacExample {
    private static final String ALGORITHM = "HmacSHA256";

    public static byte[] sign(String message, byte[] key) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(key, ALGORITHM));
        return mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean verify(String message, byte[] key, String tagBase64)
            throws Exception {
        try {
            byte[] received = Base64.getDecoder().decode(tagBase64);
            byte[] calculated = sign(message, key);
            return MessageDigest.isEqual(received, calculated);
        } catch (IllegalArgumentException invalidBase64) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        byte[] sharedKey = new byte[32];
        new SecureRandom().nextBytes(sharedKey);
        String message = "amount=10000&account=1234567890";
        String tag = Base64.getEncoder().encodeToString(sign(message, sharedKey));

        System.out.println("원본 검증: " + verify(message, sharedKey, tag));
        System.out.println("변조 검증: " + verify(
                "amount=90000&account=1234567890", sharedKey, tag));
        // HMAC은 내용을 숨기는 암호화가 아니라 무결성과 키 보유를 확인합니다.
    }
}
