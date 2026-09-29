package com.hyo.cryptolab.hmac.controller;

import com.hyo.cryptolab.hmac.service.HmacService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/hmac")
public class HmacController {

    private final HmacService hmacService;

    public HmacController(HmacService hmacService) {
        this.hmacService = hmacService;
    }

    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody Map<String, String> request) {
        String signature = hmacService.generate(request.get("message"), request.get("secretKey"));
        return Map.of("signature", signature);
    }

    @PostMapping("/verify")
    public Map<String, Object> verify(@RequestBody Map<String, String> request) {
        boolean valid = hmacService.verify(
                request.get("message"),
                request.get("secretKey"),
                request.get("signature")
        );
        return Map.of("valid", valid);
    }
}
