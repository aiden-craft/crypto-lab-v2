import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** LAB 09: 같은 환경에서 평균 시간을 비교하는 최소 측정 패턴. */
public final class BenchmarkExample {
    private static volatile byte[] sink; // 결과가 사용되지 않아 제거되는 것 방지

    @FunctionalInterface
    interface Operation { byte[] run() throws Exception; }

    public static double averageSeconds(Operation operation, int warmup, int repeats)
            throws Exception {
        if (warmup < 0 || repeats < 1) throw new IllegalArgumentException();
        for (int i = 0; i < warmup; i++) sink = operation.run();
        long start = System.nanoTime();
        for (int i = 0; i < repeats; i++) sink = operation.run();
        long elapsed = System.nanoTime() - start;
        return elapsed / 1_000_000_000.0 / repeats;
    }

    public static void main(String[] args) throws Exception {
        byte[] input = "HelloCryptoLab".getBytes(StandardCharsets.UTF_8);
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        int iterations = 20_000;
        int repeats = 10;

        double sha = averageSeconds(() -> MessageDigest.getInstance("SHA-256")
                .digest(input), 3, repeats);
        double pbkdf2 = averageSeconds(() -> {
            PBEKeySpec spec = new PBEKeySpec(
                    "practice-only".toCharArray(), salt, iterations, 256);
            try {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword();
            }
        }, 3, repeats);

        System.out.printf("SHA-256 평균: %.6f초%n", sha);
        System.out.printf("PBKDF2 평균: %.6f초%n", pbkdf2);
        System.out.printf("PBKDF2가 약 %.1f배 느림%n", pbkdf2 / sha);
        // 예열, 입력 크기, 호출 범위를 맞추고 여러 번 재측정하세요.
        // 느린 PBKDF2는 추측 비용을 높이지만 서비스 부하도 높입니다.
    }
}
