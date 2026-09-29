package com.hyo.cryptolab.benchmark.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class BenchmarkRequest {
    @Min(1)
    @Max(100)
    private int repeatCount = 10;

    @NotBlank
    private String plainText;

    @NotBlank
    private String secretKey;

    @NotBlank
    private String hmacKey;

    @Min(1000)
    @Max(100000)
    private int pbkdf2Iterations = 20000;

    public int getRepeatCount() { return repeatCount; }
    public void setRepeatCount(int repeatCount) { this.repeatCount = repeatCount; }
    public String getPlainText() { return plainText; }
    public void setPlainText(String plainText) { this.plainText = plainText; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public String getHmacKey() { return hmacKey; }
    public void setHmacKey(String hmacKey) { this.hmacKey = hmacKey; }
    public int getPbkdf2Iterations() { return pbkdf2Iterations; }
    public void setPbkdf2Iterations(int pbkdf2Iterations) { this.pbkdf2Iterations = pbkdf2Iterations; }
}
