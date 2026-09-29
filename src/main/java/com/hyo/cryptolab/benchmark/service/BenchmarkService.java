package com.hyo.cryptolab.benchmark.service;

import java.util.Map;

public interface BenchmarkService {

    Map<String, Object> compareAesAndRsa(int repeatCount, String plainText);

    Map<String, Object> compareAesAndRsa(int repeatCount, String plainText, String secretKey);

    Map<String, Object> runFullBenchmark(
            int repeatCount,
            String plainText,
            String secretKey,
            String hmacKey,
            int pbkdf2Iterations
    );
}
