package com.hyo.cryptolab.password.dto;

public class Pbkdf2VerifyResponse {

    private boolean valid;

    public Pbkdf2VerifyResponse(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }
}
