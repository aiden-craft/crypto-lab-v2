import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/** LAB 06: RSA-2048 OAEP(SHA-256/MGF1-SHA-256) 예제. */
public final class RsaOaepExample {
    private static final OAEPParameterSpec OAEP = new OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
        byte[] bytes = plainText.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 190) { // 2048비트 키, SHA-256 OAEP 기준
            throw new IllegalArgumentException("최대 190바이트를 입력하세요.");
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, OAEP);
        return Base64.getEncoder().encodeToString(cipher.doFinal(bytes));
    }

    public static String decrypt(String encoded, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP);
        byte[] plain = cipher.doFinal(Base64.getDecoder().decode(encoded));
        return new String(plain, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keys = generator.generateKeyPair();
        String first = encrypt("Hello RSA", keys.getPublic());
        String second = encrypt("Hello RSA", keys.getPublic());

        System.out.println("암호문이 다른가: " + !first.equals(second));
        System.out.println("복호화: " + decrypt(first, keys.getPrivate()));
        // 큰 데이터는 AES로 암호화하고 RSA는 작은 비밀키 보호에 사용합니다.
    }
}
