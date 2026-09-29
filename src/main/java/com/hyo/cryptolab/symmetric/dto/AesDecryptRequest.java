package com.hyo.cryptolab.symmetric.dto;

import jakarta.validation.constraints.NotBlank;

public class AesDecryptRequest {

    @NotBlank
    private String cipherText;

    @NotBlank
    private String secretKey;

    @NotBlank
    private String ivBase64;

    public String getCipherText() {
        return cipherText;
    }

    public void setCipherText(String cipherText) {
        this.cipherText = cipherText;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getIvBase64() {
        return ivBase64;
    }

    public void setIvBase64(String ivBase64) {
        this.ivBase64 = ivBase64;
    }
}
