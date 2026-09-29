package com.hyo.cryptolab.asymmetric.service;

import java.util.Map;

public interface RsaService {
    Map<String, String> generateKeyPair();
    String encrypt(String plainText, String publicKeyBase64);
    String decrypt(String cipherTextBase64, String privateKeyBase64);
}
