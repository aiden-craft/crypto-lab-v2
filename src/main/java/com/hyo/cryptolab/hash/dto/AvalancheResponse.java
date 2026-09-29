package com.hyo.cryptolab.hash.dto;

public class AvalancheResponse {

    private String originalInput;
    private String modifiedInput;
    private String originalHash;
    private String modifiedHash;
    private int differentHexChars;
    private int totalHexChars;
    private int differentBits;
    private int totalBits;

    public AvalancheResponse(
            String originalInput,
            String modifiedInput,
            String originalHash,
            String modifiedHash,
            int differentHexChars,
            int totalHexChars,
            int differentBits,
            int totalBits
    ) {
        this.originalInput = originalInput;
        this.modifiedInput = modifiedInput;
        this.originalHash = originalHash;
        this.modifiedHash = modifiedHash;
        this.differentHexChars = differentHexChars;
        this.totalHexChars = totalHexChars;
        this.differentBits = differentBits;
        this.totalBits = totalBits;
    }

    public String getOriginalInput() {
        return originalInput;
    }

    public String getModifiedInput() {
        return modifiedInput;
    }

    public String getOriginalHash() {
        return originalHash;
    }

    public String getModifiedHash() {
        return modifiedHash;
    }

    public int getDifferentHexChars() {
        return differentHexChars;
    }

    public int getTotalHexChars() {
        return totalHexChars;
    }

    public int getDifferentBits() {
        return differentBits;
    }

    public int getTotalBits() {
        return totalBits;
    }
}
