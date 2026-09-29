package com.hyo.cryptolab.service;

import com.hyo.cryptolab.asymmetric.service.RsaService;
import com.hyo.cryptolab.asymmetric.service.RsaServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

class RsaServiceTest {

    private RsaService rsaService;

    @BeforeEach
    void setUp() {
        rsaService = new RsaServiceImpl();
    }

    @Test
    void shouldEncryptAndDecryptSuccessfully() {
        Map<String, String> keyPair = rsaService.generateKeyPair();
        String cipherText = rsaService.encrypt("hello-rsa", keyPair.get("publicKey"));
        String plainText = rsaService.decrypt(cipherText, keyPair.get("privateKey"));

        Assertions.assertEquals("hello-rsa", plainText);
    }
}
