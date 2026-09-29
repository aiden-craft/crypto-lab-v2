package com.hyo.cryptolab.common.util;

import java.util.Base64;

public final class Base64Util {

    private Base64Util() {
    }

    public static String encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    public static byte[] decode(String value) {
        return Base64.getDecoder().decode(value);
    }
}
