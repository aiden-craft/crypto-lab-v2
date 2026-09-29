package com.hyo.cryptolab.password.controller;

import com.hyo.cryptolab.password.dto.Pbkdf2GenerateRequest;
import com.hyo.cryptolab.password.dto.Pbkdf2GenerateResponse;
import com.hyo.cryptolab.password.dto.Pbkdf2VerifyRequest;
import com.hyo.cryptolab.password.dto.Pbkdf2VerifyResponse;
import com.hyo.cryptolab.password.service.PasswordHashService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password")
public class PasswordController {

    private final PasswordHashService passwordHashService;

    public PasswordController(PasswordHashService passwordHashService) {
        this.passwordHashService = passwordHashService;
    }

    @PostMapping("/pbkdf2/generate")
    public Pbkdf2GenerateResponse generate(@Valid @RequestBody Pbkdf2GenerateRequest request) {
        return passwordHashService.generatePbkdf2(
                request.getPassword(),
                request.getSalt(),
                request.getIterations()
        );
    }

    @PostMapping("/pbkdf2/verify")
    public Pbkdf2VerifyResponse verify(@Valid @RequestBody Pbkdf2VerifyRequest request) {
        boolean valid = passwordHashService.verifyPbkdf2(
                request.getPassword(),
                request.getEncodedValue()
        );
        return new Pbkdf2VerifyResponse(valid);
    }
}
