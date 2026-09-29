package com.hyo.cryptolab.hash.support;

public enum HashAlgorithm {
    SHA_256("SHA-256"),
    SHA_512("SHA-512");

    private final String algorithm;

    HashAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
