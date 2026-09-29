package com.hyo.cryptolab.symmetric.controller;

import com.hyo.cryptolab.symmetric.dto.AesDecryptRequest;
import com.hyo.cryptolab.symmetric.dto.AesEncryptRequest;
import com.hyo.cryptolab.symmetric.dto.AesResponse;
import com.hyo.cryptolab.symmetric.service.AesService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/aes")
public class AesController {

    private final AesService aesService;

    public AesController(AesService aesService) {
        this.aesService = aesService;
    }

    @PostMapping("/encrypt")
    public AesResponse encrypt(@Valid @RequestBody AesEncryptRequest request) {
        return aesService.encrypt(
                request.getPlainText(),
                request.getSecretKey(),
                request.getIvMode(),
                request.getIvSeed()
        );
    }

    @PostMapping("/decrypt")
    public Map<String, Object> decrypt(@Valid @RequestBody AesDecryptRequest request) {
        String plainText = aesService.decrypt(
                request.getCipherText(),
                request.getSecretKey(),
                request.getIvBase64()
        );
        return Map.of("plainText", plainText);
    }
}
