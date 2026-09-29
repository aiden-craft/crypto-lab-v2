package com.hyo.cryptolab.hash.dto;

public class HashResponse {

    private String algorithm;
    private String plainText;
    private String salt;
    private String hashValue;

    public HashResponse(String algorithm, String plainText, String salt, String hashValue) {
        this.algorithm = algorithm;
        this.plainText = plainText;
        this.salt = salt;
        this.hashValue = hashValue;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getPlainText() {
        return plainText;
    }

    public String getSalt() {
        return salt;
    }

    public String getHashValue() {
        return hashValue;
    }
}

