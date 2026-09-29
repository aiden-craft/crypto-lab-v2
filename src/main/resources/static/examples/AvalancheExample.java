import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** LAB 02: 두 해시의 서로 다른 '비트'를 XOR로 세는 예제. */
public final class AvalancheExample {
    public static byte[] sha256(String text) throws Exception {
        return MessageDigest.getInstance("SHA-256")
                .digest(text.getBytes(StandardCharsets.UTF_8));
    }

    public static int changedBits(byte[] first, byte[] second) {
        if (first.length != second.length) {
            throw new IllegalArgumentException("같은 길이의 해시를 비교하세요.");
        }
        int changed = 0;
        for (int i = 0; i < first.length; i++) {
            changed += Integer.bitCount((first[i] ^ second[i]) & 0xff);
        }
        return changed;
    }

    public static void main(String[] args) throws Exception {
        byte[] original = sha256("hello123");
        byte[] modified = sha256("hello124");
        int changed = changedBits(original, modified);
        int totalBits = original.length * Byte.SIZE;

        System.out.printf("변경 비트: %d / %d (%.2f%%)%n",
                changed, totalBits, changed * 100.0 / totalBits);
        // 16진수 문자가 몇 개 다른지와 비트 변화율은 서로 다른 값입니다.
    }
}
