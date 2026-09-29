package com.hyo.cryptolab.symmetric.dto;

import jakarta.validation.constraints.NotBlank;

public class AesEncryptRequest {

    @NotBlank
    private String plainText;

    @NotBlank
    private String secretKey;

    private String ivMode = "RANDOM";
    private String ivSeed;

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getIvMode() {
        return ivMode;
    }

    public void setIvMode(String ivMode) {
        this.ivMode = ivMode;
    }

    public String getIvSeed() {
        return ivSeed;
    }

    public void setIvSeed(String ivSeed) {
        this.ivSeed = ivSeed;
    }
}