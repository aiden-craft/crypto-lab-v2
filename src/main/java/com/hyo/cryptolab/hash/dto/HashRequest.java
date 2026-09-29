package com.hyo.cryptolab.hash.dto;

import jakarta.validation.constraints.NotBlank;

public class HashRequest {

    @NotBlank
    private String plainText;

    private String salt;

    private String algorithm = "SHA_256";

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
