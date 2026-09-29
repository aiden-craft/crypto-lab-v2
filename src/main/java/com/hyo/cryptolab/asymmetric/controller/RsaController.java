package com.hyo.cryptolab.asymmetric.controller;

import com.hyo.cryptolab.asymmetric.service.RsaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rsa")
public class RsaController {

    private final RsaService rsaService;

    public RsaController(RsaService rsaService) {
        this.rsaService = rsaService;
    }

    @PostMapping("/keys")
    public Map<String, String> generateKeys() {
        return rsaService.generateKeyPair();
    }

    @PostMapping("/encrypt")
    public Map<String, String> encrypt(@RequestBody Map<String, String> request) {
        String cipherText = rsaService.encrypt(request.get("plainText"), request.get("publicKey"));
        return Map.of("cipherText", cipherText);
    }

    @PostMapping("/decrypt")
    public Map<String, String> decrypt(@RequestBody Map<String, String> request) {
        String plainText = rsaService.decrypt(request.get("cipherText"), request.get("privateKey"));
        return Map.of("plainText", plainText);
    }
}
