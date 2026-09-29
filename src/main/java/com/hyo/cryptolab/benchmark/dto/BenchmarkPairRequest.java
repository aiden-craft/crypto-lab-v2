package com.hyo.cryptolab.benchmark.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class BenchmarkPairRequest {
    @Min(1)
    @Max(100)
    private int repeatCount = 10;

    @NotBlank
    private String plainText;

    @NotBlank
    private String secretKey;

    public int getRepeatCount() { return repeatCount; }
    public void setRepeatCount(int repeatCount) { this.repeatCount = repeatCount; }
    public String getPlainText() { return plainText; }
    public void setPlainText(String plainText) { this.plainText = plainText; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
}
