package com.hyo.cryptolab.service;

import com.hyo.cryptolab.asymmetric.service.RsaService;
import com.hyo.cryptolab.asymmetric.service.RsaServiceImpl;
import com.hyo.cryptolab.benchmark.service.BenchmarkService;
import com.hyo.cryptolab.benchmark.service.BenchmarkServiceImpl;
import com.hyo.cryptolab.hash.service.HashService;
import com.hyo.cryptolab.hash.service.HashServiceImpl;
import com.hyo.cryptolab.hmac.service.HmacService;
import com.hyo.cryptolab.hmac.service.HmacServiceImpl;
import com.hyo.cryptolab.password.service.PasswordHashService;
import com.hyo.cryptolab.password.service.PasswordHashServiceImpl;
import com.hyo.cryptolab.symmetric.service.AesService;
import com.hyo.cryptolab.symmetric.service.AesServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

class BenchmarkServiceTest {

    private BenchmarkService benchmarkService;

    @BeforeEach
    void setUp() {
        AesService aesService = new AesServiceImpl();
        RsaService rsaService = new RsaServiceImpl();
        HashService hashService = new HashServiceImpl();
        HmacService hmacService = new HmacServiceImpl();
        PasswordHashService passwordHashService = new PasswordHashServiceImpl();

        benchmarkService = new BenchmarkServiceImpl(
                aesService,
                rsaService,
                hashService,
                hmacService,
                passwordHashService
        );
    }

    @Test
    void shouldReturnAesRsaComparisonMetrics() {
        Map<String, Object> result = benchmarkService.compareAesAndRsa(5, "HelloCryptoLab");

        Assertions.assertTrue(result.containsKey("aesEncryptElapsedNanos"));
        Assertions.assertTrue(result.containsKey("rsaEncryptElapsedNanos"));
        Assertions.assertEquals(5, result.get("repeatCount"));
    }

    @Test
    void shouldCompareWithEditedAesKey() {
        Map<String, Object> result = benchmarkService.compareAesAndRsa(2, "HelloCryptoLab", "edited-lab-key");

        Assertions.assertEquals(2, result.get("repeatCount"));
        Assertions.assertTrue((double) result.get("aesEncryptAverageNanos") >= 0);
        Assertions.assertTrue((double) result.get("rsaDecryptAverageNanos") >= 0);
    }

    @Test
    void shouldReturnFullBenchmarkMetrics() {
        Map<String, Object> result = benchmarkService.runFullBenchmark(
                3,
                "HelloCryptoLab",
                "benchmark-secret-key",
                "benchmark-hmac-key",
                1000
        );

        Assertions.assertTrue(result.containsKey("hash"));
        Assertions.assertTrue(result.containsKey("aes"));
        Assertions.assertTrue(result.containsKey("rsa"));
        Assertions.assertTrue(result.containsKey("hmac"));
        Assertions.assertTrue(result.containsKey("pbkdf2"));
        Assertions.assertTrue(((String) result.get("summary")).contains("오프라인 비밀번호 추측"));
    }
}
