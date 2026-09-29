import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/** LAB 01: Salt를 입력에 섞어 SHA-256 결과를 비교하는 독립 예제. */
public final class HashSaltExample {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String sha256(String text, byte[] salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    public static void main(String[] args) throws Exception {
        String input = "password123"; // 실습용 값
        byte[] saltA = new byte[16];
        byte[] saltB = new byte[16];
        RANDOM.nextBytes(saltA);
        RANDOM.nextBytes(saltB);

        System.out.println("Salt 없음: " + sha256(input, new byte[0]));
        System.out.println("Salt A:    " + sha256(input, saltA));
        System.out.println("Salt B:    " + sha256(input, saltB));
        // 세 결과 모두 256비트 = 32바이트 = 16진수 64자리입니다.
        // 비밀번호 저장에는 단순 SHA-256 대신 PBKDF2 같은 전용 함수를 사용합니다.
    }
}
