package com.hyo.cryptolab.symmetric.dto;

public class AesResponse {

    private String plainText;
    private String cipherText;
    private String keyHex;
    private String ivBase64;
    private String mode;
    private String note;

    public AesResponse(String plainText, String cipherText, String keyHex, String ivBase64, String mode, String note) {
        this.plainText = plainText;
        this.cipherText = cipherText;
        this.keyHex = keyHex;
        this.ivBase64 = ivBase64;
        this.mode = mode;
        this.note = note;
    }

    public String getPlainText() {
        return plainText;
    }

    public String getCipherText() {
        return cipherText;
    }

    public String getKeyHex() {
        return keyHex;
    }

    public String getIvBase64() {
        return ivBase64;
    }

    public String getMode() {
        return mode;
    }

    public String getNote() {
        return note;
    }
}
