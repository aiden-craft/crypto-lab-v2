package com.hyo.cryptolab.service;

import com.hyo.cryptolab.password.dto.Pbkdf2GenerateResponse;
import com.hyo.cryptolab.password.service.PasswordHashService;
import com.hyo.cryptolab.password.service.PasswordHashServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordHashServiceTest {

    private PasswordHashService passwordHashService;

    @BeforeEach
    void setUp() {
        passwordHashService = new PasswordHashServiceImpl();
    }

    @Test
    void shouldGenerateAndVerifyPbkdf2() {
        Pbkdf2GenerateResponse response = passwordHashService.generatePbkdf2("password123", null, 60000);
        boolean valid = passwordHashService.verifyPbkdf2("password123", response.getEncodedValue());

        Assertions.assertTrue(valid);
    }

    @Test
    void shouldFailWhenPasswordDoesNotMatch() {
        Pbkdf2GenerateResponse response = passwordHashService.generatePbkdf2("password123", null, 60000);
        boolean valid = passwordHashService.verifyPbkdf2("wrong-password", response.getEncodedValue());

        Assertions.assertFalse(valid);
    }
}
