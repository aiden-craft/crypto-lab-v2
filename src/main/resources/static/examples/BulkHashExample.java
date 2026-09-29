import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

/** LAB 03: 입력 중복과 해시 충돌을 분리해 기록하는 예제. */
public final class BulkHashExample {
    public static void main(String[] args) throws Exception {
        int count = 10_000;
        SecureRandom random = new SecureRandom();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        HexFormat hex = HexFormat.of();
        Set<String> distinctInputs = new HashSet<>();
        Map<String, String> firstInputForHash = new HashMap<>();
        int collisions = 0;

        for (int i = 0; i < count; i++) {
            byte[] input = new byte[12];
            random.nextBytes(input);
            String inputHex = hex.formatHex(input);
            String hashHex = hex.formatHex(sha256.digest(input));

            distinctInputs.add(inputHex);
            String previousInput = firstInputForHash.putIfAbsent(hashHex, inputHex);
            if (previousInput != null && !previousInput.equals(inputHex)) {
                collisions++; // 서로 다른 입력이 같은 해시를 만든 경우만 충돌
            }
        }

        System.out.println("시도: " + count);
        System.out.println("서로 다른 입력: " + distinctInputs.size());
        System.out.println("서로 다른 해시: " + firstInputForHash.size());
        System.out.println("관찰된 충돌: " + collisions);
        // 작은 실험에서 충돌이 없었다고 충돌 가능성이 0이 되는 것은 아닙니다.
    }
}
