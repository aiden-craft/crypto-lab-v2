package com.hyo.cryptolab.password.dto;

public class Pbkdf2GenerateResponse {

    private String algorithm;
    private int iterations;
    private String saltBase64;
    private String hashBase64;
    private String encodedValue;

    public Pbkdf2GenerateResponse(String algorithm, int iterations, String saltBase64, String hashBase64, String encodedValue) {
        this.algorithm = algorithm;
        this.iterations = iterations;
        this.saltBase64 = saltBase64;
        this.hashBase64 = hashBase64;
        this.encodedValue = encodedValue;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getIterations() {
        return iterations;
    }

    public String getSaltBase64() {
        return saltBase64;
    }

    public String getHashBase64() {
        return hashBase64;
    }

    public String getEncodedValue() {
        return encodedValue;
    }
}
