package com.hyo.cryptolab.common.util;

public final class HexUtil {

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private HexUtil() {
    }

    public static String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            chars[i * 2] = HEX_ARRAY[value >>> 4];
            chars[i * 2 + 1] = HEX_ARRAY[value & 0x0F];
        }
        return new String(chars);
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Hex string length must be even.");
        }

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return data;
    }
}
