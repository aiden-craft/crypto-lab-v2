package com.hyo.cryptolab.symmetric.service;

import com.hyo.cryptolab.symmetric.dto.AesResponse;

public interface AesService {
    AesResponse encrypt(String plainText, String secretKey, String ivMode, String ivSeed);
    String decrypt(String cipherTextBase64, String secretKey, String ivBase64);
}
