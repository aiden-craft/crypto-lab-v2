package com.hyo.cryptolab.service;

import com.hyo.cryptolab.hmac.service.HmacService;
import com.hyo.cryptolab.hmac.service.HmacServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HmacServiceTest {

    private HmacService hmacService;

    @BeforeEach
    void setUp() {
        hmacService = new HmacServiceImpl();
    }

    @Test
    void shouldGenerateAndVerifySuccessfully() {
        String signature = hmacService.generate("message", "secret");
        boolean valid = hmacService.verify("message", "secret", signature);

        Assertions.assertTrue(valid);
    }

    @Test
    void shouldFailWhenMessageChanges() {
        String signature = hmacService.generate("message", "secret");
        boolean valid = hmacService.verify("message2", "secret", signature);

        Assertions.assertFalse(valid);
    }
}
