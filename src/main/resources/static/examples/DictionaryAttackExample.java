import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/** LAB 04: 미리 계산한 해시와 대조하는 사전 공격 원리 예제. */
public final class DictionaryAttackExample {
    public static String sha256(String input) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(bytes);
    }

    public static Map<String, String> buildDictionary(List<String> candidates)
            throws Exception {
        Map<String, String> byHash = new HashMap<>();
        for (String candidate : candidates) {
            byHash.put(sha256(candidate), candidate);
        }
        return byHash;
    }

    public static void main(String[] args) throws Exception {
        List<String> samples = List.of("123456", "password", "password123");
        Map<String, String> dictionary = buildDictionary(samples);

        String targetHash = sha256("password123");
        System.out.println("Salt 없음: " + dictionary.get(targetHash));

        String saltedHash = sha256("training-salt:" + "password123");
        System.out.println("Salt 적용: " + dictionary.get(saltedHash)); // null
        // 해시를 복호화한 것이 아니라, 준비된 후보의 해시와 일치시킨 것입니다.
        // 이 예제는 해시 체인을 사용하는 정식 Rainbow Table은 아닙니다.
    }
}
