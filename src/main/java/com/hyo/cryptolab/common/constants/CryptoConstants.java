package com.hyo.cryptolab.common.constants;

public final class CryptoConstants {

    private CryptoConstants() {
    }

    public static final String SHA_256 = "SHA-256";
    public static final String SHA_512 = "SHA-512";

    public static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    public static final String AES_ALGORITHM = "AES";

    public static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    public static final String RSA_ALGORITHM = "RSA";

    public static final String HMAC_SHA256 = "HmacSHA256";

    public static final int AES_KEY_SIZE = 32; // 256-bit
    public static final int AES_IV_SIZE = 16;  // 128-bit
    public static final int RSA_KEY_SIZE = 2048;
}
