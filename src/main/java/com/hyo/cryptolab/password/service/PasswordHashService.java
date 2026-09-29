package com.hyo.cryptolab.password.service;

import com.hyo.cryptolab.password.dto.Pbkdf2GenerateResponse;

public interface PasswordHashService {
    Pbkdf2GenerateResponse generatePbkdf2(String password, String salt, int iterations);
    boolean verifyPbkdf2(String password, String encodedValue);
}
