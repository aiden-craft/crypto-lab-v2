package com.hyo.cryptolab.common.util;

import com.hyo.cryptolab.common.constants.CryptoConstants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

public final class IvDerivationUtil {

    private IvDerivationUtil() {
    }

    public static byte[] deriveIvFromDomain(String domainSeed) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(domainSeed.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(hash, CryptoConstants.AES_IV_SIZE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to derive IV from domain seed.", e);
        }
    }
}
