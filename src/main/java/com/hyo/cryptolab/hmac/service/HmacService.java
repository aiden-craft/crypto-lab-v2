package com.hyo.cryptolab.hmac.service;

public interface HmacService {
    String generate(String message, String secretKey);
    boolean verify(String message, String secretKey, String signature);
}
