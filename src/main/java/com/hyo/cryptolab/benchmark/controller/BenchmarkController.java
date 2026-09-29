package com.hyo.cryptolab.benchmark.controller;

import com.hyo.cryptolab.benchmark.dto.BenchmarkRequest;
import com.hyo.cryptolab.benchmark.dto.BenchmarkPairRequest;
import com.hyo.cryptolab.benchmark.service.BenchmarkService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/benchmark")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    public BenchmarkController(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @GetMapping("/aes-rsa")
    public Map<String, Object> compare(
            @RequestParam(defaultValue = "100") int repeatCount,
            @RequestParam(defaultValue = "HelloCryptoLab") String plainText
    ) {
        return benchmarkService.compareAesAndRsa(repeatCount, plainText);
    }

    @PostMapping("/aes-rsa")
    public Map<String, Object> comparePost(@Valid @RequestBody BenchmarkPairRequest request) {
        return benchmarkService.compareAesAndRsa(
                request.getRepeatCount(), request.getPlainText(), request.getSecretKey());
    }

    @GetMapping("/full")
    public Map<String, Object> fullBenchmark(
            @RequestParam(defaultValue = "100") int repeatCount,
            @RequestParam(defaultValue = "HelloCryptoLab") String plainText,
            @RequestParam(defaultValue = "benchmark-secret-key") String secretKey,
            @RequestParam(defaultValue = "benchmark-hmac-key") String hmacKey,
            @RequestParam(defaultValue = "60000") int pbkdf2Iterations
    ) {
        return benchmarkService.runFullBenchmark(
                repeatCount,
                plainText,
                secretKey,
                hmacKey,
                pbkdf2Iterations
        );
    }

    @PostMapping("/full")
    public Map<String, Object> fullBenchmarkPost(@Valid @RequestBody BenchmarkRequest request) {
        return benchmarkService.runFullBenchmark(
                request.getRepeatCount(),
                request.getPlainText(),
                request.getSecretKey(),
                request.getHmacKey(),
                request.getPbkdf2Iterations()
        );
    }
}
