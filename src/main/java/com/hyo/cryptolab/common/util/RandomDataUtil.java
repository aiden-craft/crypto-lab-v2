package com.hyo.cryptolab.common.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public final class RandomDataUtil {

    private static final String CHAR_POOL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*";
    private static final SecureRandom RANDOM = new SecureRandom();

    private RandomDataUtil() {
    }

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_POOL.charAt(RANDOM.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    public static byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    public static List<String> generateRandomStrings(int count, int length) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(randomString(length));
        }
        return result;
    }
}
