package com.hyo.cryptolab.asymmetric.service;

import com.hyo.cryptolab.common.constants.CryptoConstants;
import com.hyo.cryptolab.common.util.Base64Util;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

@Service
public class RsaServiceImpl implements RsaService {

    @Override
    public Map<String, String> generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(CryptoConstants.RSA_ALGORITHM);
            generator.initialize(CryptoConstants.RSA_KEY_SIZE);
            KeyPair keyPair = generator.generateKeyPair();

            return Map.of(
                    "publicKey", Base64Util.encode(keyPair.getPublic().getEncoded()),
                    "privateKey", Base64Util.encode(keyPair.getPrivate().getEncoded())
            );
        } catch (Exception e) {
            throw new IllegalStateException("RSA key generation failed.", e);
        }
    }

    @Override
    public String encrypt(String plainText, String publicKeyBase64) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(CryptoConstants.RSA_ALGORITHM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64Util.decode(publicKeyBase64));

            Cipher cipher = Cipher.getInstance(CryptoConstants.RSA_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(spec));

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64Util.encode(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("RSA encryption failed.", e);
        }
    }

    @Override
    public String decrypt(String cipherTextBase64, String privateKeyBase64) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(CryptoConstants.RSA_ALGORITHM);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64Util.decode(privateKeyBase64));

            Cipher cipher = Cipher.getInstance(CryptoConstants.RSA_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keyFactory.generatePrivate(spec));

            byte[] decrypted = cipher.doFinal(Base64Util.decode(cipherTextBase64));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("RSA decryption failed.", e);
        }
    }
}
