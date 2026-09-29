package com.hyo.cryptolab.password.service;

import com.hyo.cryptolab.common.util.Base64Util;
import com.hyo.cryptolab.common.util.RandomDataUtil;
import com.hyo.cryptolab.password.dto.Pbkdf2GenerateResponse;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

@Service
public class PasswordHashServiceImpl implements PasswordHashService {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    @Override
    public Pbkdf2GenerateResponse generatePbkdf2(String password, String salt, int iterations) {
        try {
            byte[] saltBytes = (salt == null || salt.isBlank())
                    ? RandomDataUtil.randomBytes(SALT_LENGTH)
                    : salt.getBytes(StandardCharsets.UTF_8);

            byte[] hashBytes = pbkdf2(password.toCharArray(), saltBytes, iterations, KEY_LENGTH);

            String saltBase64 = Base64Util.encode(saltBytes);
            String hashBase64 = Base64Util.encode(hashBytes);
            String encodedValue = String.format("%s$%d$%s$%s", ALGORITHM, iterations, saltBase64, hashBase64);

            return new Pbkdf2GenerateResponse(
                    ALGORITHM,
                    iterations,
                    saltBase64,
                    hashBase64,
                    encodedValue
            );
        } catch (Exception e) {
            throw new IllegalStateException("PBKDF2 generation failed.", e);
        }
    }

    @Override
    public boolean verifyPbkdf2(String password, String encodedValue) {
        try {
            String[] parts = encodedValue.split("\\$");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid encoded PBKDF2 format.");
            }

            String algorithm = parts[0];
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 1_000 || iterations > 600_000) {
                throw new IllegalArgumentException("PBKDF2 iterations must be 1000..600000.");
            }
            byte[] salt = Base64Util.decode(parts[2]);
            byte[] expectedHash = Base64Util.decode(parts[3]);

            if (!ALGORITHM.equals(algorithm)) {
                throw new IllegalArgumentException("Unsupported PBKDF2 algorithm: " + algorithm);
            }

            byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations, expectedHash.length * 8);

            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            throw new IllegalStateException("PBKDF2 verification failed.", e);
        }
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] encoded = factory.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return Arrays.copyOf(encoded, encoded.length);
    }
}
