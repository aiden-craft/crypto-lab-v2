import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** LAB 08: Salt와 반복 횟수를 함께 저장하는 PBKDF2 예제. */
public final class Pbkdf2Example {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 60_000; // 운영 값은 환경에 맞게 측정
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static byte[] derive(char[] password, byte[] salt, int iterations)
            throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM)
                    .generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    public static String encode(char[] password) throws Exception {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = derive(password, salt, ITERATIONS);
        Base64.Encoder base64 = Base64.getEncoder();
        return ALGORITHM + "$" + ITERATIONS + "$"
                + base64.encodeToString(salt) + "$" + base64.encodeToString(hash);
    }

    public static boolean verify(char[] password, String stored) throws Exception {
        String[] parts = stored.split("\\$", -1);
        if (parts.length != 4 || !ALGORITHM.equals(parts[0])) return false;
        try {
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 1_000 || iterations > 600_000) return false;
            Base64.Decoder base64 = Base64.getDecoder();
            byte[] salt = base64.decode(parts[2]);
            byte[] expected = base64.decode(parts[3]);
            if (salt.length != 16 || expected.length != KEY_BITS / 8) return false;
            return MessageDigest.isEqual(expected, derive(password, salt, iterations));
        } catch (IllegalArgumentException malformed) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        char[] password = "practice-only".toCharArray();
        try {
            String first = encode(password);
            String second = encode(password);
            System.out.println("서로 다른 저장 값: " + !first.equals(second));
            System.out.println("원본 검증: " + verify(password, first));
            System.out.println("다른 값 검증: " + verify("wrong".toCharArray(), first));
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
