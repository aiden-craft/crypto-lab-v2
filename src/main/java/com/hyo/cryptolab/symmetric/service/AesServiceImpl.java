package com.hyo.cryptolab.symmetric.service;

import com.hyo.cryptolab.common.constants.CryptoConstants;
import com.hyo.cryptolab.common.util.Base64Util;
import com.hyo.cryptolab.common.util.HexUtil;
import com.hyo.cryptolab.common.util.IvDerivationUtil;
import com.hyo.cryptolab.common.util.RandomDataUtil;
import com.hyo.cryptolab.symmetric.dto.AesResponse;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

@Service
public class AesServiceImpl implements AesService {

    @Override
    public AesResponse encrypt(String plainText, String secretKey, String ivMode, String ivSeed) {
        try {
            byte[] key = normalizeKey(secretKey);
            byte[] iv = "DERIVED".equalsIgnoreCase(ivMode)
                    ? IvDerivationUtil.deriveIvFromDomain(ivSeed == null ? "default-domain" : ivSeed)
                    : RandomDataUtil.randomBytes(CryptoConstants.AES_IV_SIZE);

            Cipher cipher = Cipher.getInstance(CryptoConstants.AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, CryptoConstants.AES_ALGORITHM), new IvParameterSpec(iv));

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            String note = "DERIVED".equalsIgnoreCase(ivMode)
                    ? "교육용 파생 IV 사용. 운영 환경에서는 예측 가능한 IV 대신 랜덤 IV 사용 권장."
                    : "랜덤 IV 사용.";

            return new AesResponse(
                    plainText,
                    Base64Util.encode(encrypted),
                    HexUtil.bytesToHex(key),
                    Base64Util.encode(iv),
                    ivMode,
                    note
            );
        } catch (Exception e) {
            throw new IllegalStateException("AES encryption failed.", e);
        }
    }

    @Override
    public String decrypt(String cipherTextBase64, String secretKey, String ivBase64) {
        try {
            byte[] key = normalizeKey(secretKey);
            byte[] iv = Base64Util.decode(ivBase64);
            byte[] cipherText = Base64Util.decode(cipherTextBase64);

            Cipher cipher = Cipher.getInstance(CryptoConstants.AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, CryptoConstants.AES_ALGORITHM), new IvParameterSpec(iv));

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("AES decryption failed.", e);
        }
    }

    private byte[] normalizeKey(String secretKey) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return Arrays.copyOf(digest.digest(secretKey.getBytes(StandardCharsets.UTF_8)), CryptoConstants.AES_KEY_SIZE);
    }
}
