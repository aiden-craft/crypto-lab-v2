package com.hyo.cryptolab.password.dto;

import jakarta.validation.constraints.NotBlank;

public class Pbkdf2VerifyRequest {

    @NotBlank
    private String password;

    @NotBlank
    private String encodedValue;

    public String getPassword() {
        return password;
    }

    public String getEncodedValue() {
        return encodedValue;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEncodedValue(String encodedValue) {
        this.encodedValue = encodedValue;
    }
}
