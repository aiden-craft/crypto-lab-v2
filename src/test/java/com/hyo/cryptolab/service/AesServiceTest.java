package com.hyo.cryptolab.service;

import com.hyo.cryptolab.symmetric.dto.AesResponse;
import com.hyo.cryptolab.symmetric.service.AesService;
import com.hyo.cryptolab.symmetric.service.AesServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AesServiceTest {

    private AesService aesService;

    @BeforeEach
    void setUp() {
        aesService = new AesServiceImpl();
    }

    @Test
    void shouldEncryptAndDecryptSuccessfully() {
        AesResponse encrypted = aesService.encrypt("hello-aes", "secret-key", "RANDOM", null);
        String plainText = aesService.decrypt(encrypted.getCipherText(), "secret-key", encrypted.getIvBase64());

        Assertions.assertEquals("hello-aes", plainText);
    }

    @Test
    void shouldGenerateDifferentCipherTextWithRandomIv() {
        AesResponse encrypted1 = aesService.encrypt("same-text", "secret-key", "RANDOM", null);
        AesResponse encrypted2 = aesService.encrypt("same-text", "secret-key", "RANDOM", null);

        Assertions.assertNotEquals(encrypted1.getCipherText(), encrypted2.getCipherText());
    }

    @Test
    void shouldGenerateSameCipherTextWhenDerivedIvIsSame() {
        AesResponse encrypted1 = aesService.encrypt("same-text", "secret-key", "DERIVED", "rsno");
        AesResponse encrypted2 = aesService.encrypt("same-text", "secret-key", "DERIVED", "rsno");

        Assertions.assertEquals(encrypted1.getCipherText(), encrypted2.getCipherText());
    }
}
