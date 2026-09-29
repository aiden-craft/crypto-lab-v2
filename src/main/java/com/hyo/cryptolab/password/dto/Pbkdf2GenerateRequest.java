package com.hyo.cryptolab.password.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

public class Pbkdf2GenerateRequest {

    @NotBlank
    private String password;

    private String salt;

    @Min(1000)
    @Max(600000)
    private int iterations = 60000;

    public String getPassword() {
        return password;
    }

    public String getSalt() {
        return salt;
    }

    public int getIterations() {
        return iterations;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
}
