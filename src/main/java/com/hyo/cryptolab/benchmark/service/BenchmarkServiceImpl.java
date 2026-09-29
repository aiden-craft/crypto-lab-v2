package com.hyo.cryptolab.benchmark.service;

import com.hyo.cryptolab.asymmetric.service.RsaService;
import com.hyo.cryptolab.hash.service.HashService;
import com.hyo.cryptolab.hash.support.HashAlgorithm;
import com.hyo.cryptolab.hmac.service.HmacService;
import com.hyo.cryptolab.password.dto.Pbkdf2GenerateResponse;
import com.hyo.cryptolab.password.service.PasswordHashService;
import com.hyo.cryptolab.symmetric.dto.AesResponse;
import com.hyo.cryptolab.symmetric.service.AesService;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BenchmarkServiceImpl implements BenchmarkService {

    private final AesService aesService;
    private final RsaService rsaService;
    private final HashService hashService;
    private final HmacService hmacService;
    private final PasswordHashService passwordHashService;

    public BenchmarkServiceImpl(
            AesService aesService,
            RsaService rsaService,
            HashService hashService,
            HmacService hmacService,
            PasswordHashService passwordHashService
    ) {
        this.aesService = aesService;
        this.rsaService = rsaService;
        this.hashService = hashService;
        this.hmacService = hmacService;
        this.passwordHashService = passwordHashService;
    }

    @Override
    public Map<String, Object> compareAesAndRsa(int repeatCount, String plainText) {
        return compareAesAndRsa(repeatCount, plainText, "benchmark-secret-key");
    }

    @Override
    public Map<String, Object> compareAesAndRsa(int repeatCount, String plainText, String secretKey) {
        validateRepeatCount(repeatCount);

        Map<String, String> keyPair = rsaService.generateKeyPair();

        long aesEncryptStart = System.nanoTime();
        AesResponse lastAesResponse = null;
        for (int i = 0; i < repeatCount; i++) {
            lastAesResponse = aesService.encrypt(plainText, secretKey, "RANDOM", null);
        }
        long aesEncryptElapsed = toNanos(aesEncryptStart);

        long aesDecryptStart = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) {
            aesService.decrypt(
                    lastAesResponse.getCipherText(),
                    secretKey,
                    lastAesResponse.getIvBase64()
            );
        }
        long aesDecryptElapsed = toNanos(aesDecryptStart);

        long rsaEncryptStart = System.nanoTime();
        String lastRsaCipherText = null;
        for (int i = 0; i < repeatCount; i++) {
            lastRsaCipherText = rsaService.encrypt(plainText, keyPair.get("publicKey"));
        }
        long rsaEncryptElapsed = toNanos(rsaEncryptStart);

        long rsaDecryptStart = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) {
            rsaService.decrypt(lastRsaCipherText, keyPair.get("privateKey"));
        }
        long rsaDecryptElapsed = toNanos(rsaDecryptStart);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("repeatCount", repeatCount);
        result.put("plainTextLength", plainText.length());
        result.put("aesEncryptElapsedNanos", aesEncryptElapsed);
        result.put("aesDecryptElapsedNanos", aesDecryptElapsed);
        result.put("rsaEncryptElapsedNanos", rsaEncryptElapsed);
        result.put("rsaDecryptElapsedNanos", rsaDecryptElapsed);
        result.put("aesEncryptAverageNanos", averageNanos(aesEncryptElapsed, repeatCount));
        result.put("aesDecryptAverageNanos", averageNanos(aesDecryptElapsed, repeatCount));
        result.put("rsaEncryptAverageNanos", averageNanos(rsaEncryptElapsed, repeatCount));
        result.put("rsaDecryptAverageNanos", averageNanos(rsaDecryptElapsed, repeatCount));
        result.put("summary", "일반적으로 AES가 RSA보다 훨씬 빠르며, 실데이터 암호화에는 AES가 적합합니다.");

        return result;
    }

    @Override
    public Map<String, Object> runFullBenchmark(
            int repeatCount,
            String plainText,
            String secretKey,
            String hmacKey,
            int pbkdf2Iterations
    ) {
        validateRepeatCount(repeatCount);
        if (pbkdf2Iterations < 1_000 || pbkdf2Iterations > 100_000) {
            throw new IllegalArgumentException("pbkdf2Iterations must be 1000..100000.");
        }

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("repeatCount", repeatCount);
        input.put("plainTextLength", plainText.length());
        input.put("pbkdf2Iterations", pbkdf2Iterations);

        Map<String, Object> hashMetrics = benchmarkHash(repeatCount, plainText);
        Map<String, Object> aesMetrics = benchmarkAes(repeatCount, plainText, secretKey);
        Map<String, Object> rsaMetrics = benchmarkRsa(repeatCount, plainText);
        Map<String, Object> hmacMetrics = benchmarkHmac(repeatCount, plainText, hmacKey);
        Map<String, Object> pbkdf2Metrics = benchmarkPbkdf2(repeatCount, plainText, pbkdf2Iterations);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("input", input);
        result.put("hash", hashMetrics);
        result.put("aes", aesMetrics);
        result.put("rsa", rsaMetrics);
        result.put("hmac", hmacMetrics);
        result.put("pbkdf2", pbkdf2Metrics);
        result.put("summary", "PBKDF2의 계산 비용은 오프라인 비밀번호 추측 시도당 비용을 높입니다. "
                + "반복 횟수는 로그인 서버 부하에도 영향을 줍니다. "
                + "AES와 RSA는 목적이 달라 속도만으로 보안 강도를 비교할 수 없습니다.");

        return result;
    }

    private Map<String, Object> benchmarkHash(int repeatCount, String plainText) {
        long sha256Start = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) {
            hashService.hash(plainText, null, HashAlgorithm.SHA_256);
        }
        long sha256Elapsed = toNanos(sha256Start);

        long sha512Start = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) {
            hashService.hash(plainText, null, HashAlgorithm.SHA_512);
        }
        long sha512Elapsed = toNanos(sha512Start);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sha256ElapsedNanos", sha256Elapsed);
        result.put("sha512ElapsedNanos", sha512Elapsed);
        result.put("sha256AverageNanos", averageNanos(sha256Elapsed, repeatCount));
        result.put("sha512AverageNanos", averageNanos(sha512Elapsed, repeatCount));
        result.put("note", "단순 해시는 매우 빠르지만, 비밀번호 저장에는 PBKDF2 같은 느린 방식이 더 적합합니다.");

        return result;
    }

    private Map<String, Object> benchmarkAes(int repeatCount, String plainText, String secretKey) {
        long encryptStart = System.nanoTime();
        AesResponse lastResponse = null;
        for (int i = 0; i < repeatCount; i++) {
            lastResponse = aesService.encrypt(plainText, secretKey, "RANDOM", null);
        }
        long encryptElapsed = toNanos(encryptStart);

        long decryptStart = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) {
            aesService.decrypt(lastResponse.getCipherText(), secretKey, lastResponse.getIvBase64());
        }
        long decryptElapsed = toNanos(decryptStart);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("encryptElapsedNanos", encryptElapsed);
        result.put("decryptElapsedNanos", decryptElapsed);
        result.put("encryptAverageNanos", averageNanos(encryptElapsed, repeatCount));
        result.put("decryptAverageNanos", averageNanos(decryptElapsed, repeatCount));
        result.put("sampleCipherText", lastResponse.getCipherText());
        result.put("note", "AES는 대칭키 기반으로 빠르고, 대량 데이터 암호화에 적합합니다.");

        return result;
    }

    private Map<String, Object> benchmarkRsa(int repeatCount, String plainText) {
        Map<String, String> keyPair = rsaService.generateKeyPair();

        long encryptStart = System.nanoTime();
        String lastCipherText = null;
        for (int i = 0; i < repeatCount; i++) {
            lastCipherText = rsaService.encrypt(plainText, keyPair.get("publicKey"));
        }
        long encryptElapsed = toNanos(encryptStart);

        long decryptStart = System.nanoTime();
        for (int i = 0; i < repeatCount; i++) {
            rsaService.decrypt(lastCipherText, keyPair.get("privateKey"));
        }
        long decryptElapsed = toNanos(decryptStart);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("encryptElapsedNanos", encryptElapsed);
        result.put("decryptElapsedNanos", decryptElapsed);
        result.put("encryptAverageNanos", averageNanos(encryptElapsed, repeatCount));
        result.put("decryptAverageNanos", averageNanos(decryptElapsed, repeatCount));
        result.put("sampleCipherText", lastCipherText);
        result.put("note", "RSA는 공개키/개인키 구조 이해에 적합하지만, 대량 데이터 처리에는 느립니다.");

        return result;
    }

    private Map<String, Object> benchmarkHmac(int repeatCount, String plainText, String hmacKey) {
        long generateStart = System.nanoTime();
        String signature = null;
        for (int i = 0; i < repeatCount; i++) {
            signature = hmacService.generate(plainText, hmacKey);
        }
        long generateElapsed = toNanos(generateStart);

        long verifyStart = System.nanoTime();
        boolean valid = false;
        for (int i = 0; i < repeatCount; i++) {
            valid = hmacService.verify(plainText, hmacKey, signature);
        }
        long verifyElapsed = toNanos(verifyStart);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("generateElapsedNanos", generateElapsed);
        result.put("verifyElapsedNanos", verifyElapsed);
        result.put("generateAverageNanos", averageNanos(generateElapsed, repeatCount));
        result.put("verifyAverageNanos", averageNanos(verifyElapsed, repeatCount));
        result.put("verifyResult", valid);
        result.put("sampleSignature", signature);
        result.put("note", "HMAC은 비밀키 기반 무결성 검증에 적합합니다.");

        return result;
    }

    private Map<String, Object> benchmarkPbkdf2(int repeatCount, String plainText, int pbkdf2Iterations) {
        long generateStart = System.nanoTime();
        Pbkdf2GenerateResponse lastResponse = null;
        for (int i = 0; i < repeatCount; i++) {
            lastResponse = passwordHashService.generatePbkdf2(plainText, null, pbkdf2Iterations);
        }
        long generateElapsed = toNanos(generateStart);

        long verifyStart = System.nanoTime();
        boolean valid = false;
        for (int i = 0; i < repeatCount; i++) {
            valid = passwordHashService.verifyPbkdf2(plainText, lastResponse.getEncodedValue());
        }
        long verifyElapsed = toNanos(verifyStart);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("generateElapsedNanos", generateElapsed);
        result.put("verifyElapsedNanos", verifyElapsed);
        result.put("generateAverageNanos", averageNanos(generateElapsed, repeatCount));
        result.put("verifyAverageNanos", averageNanos(verifyElapsed, repeatCount));
        result.put("verifyResult", valid);
        result.put("sampleEncodedValue", lastResponse.getEncodedValue());
        result.put("note", "PBKDF2는 비밀번호 저장용으로 의도적으로 느리게 설계된 방식입니다.");

        return result;
    }

    private long toNanos(long startNano) {
        return System.nanoTime() - startNano;
    }

    private double averageNanos(long elapsedNanos, int repeatCount) {
        if (repeatCount <= 0) return 0D;
        return (double) elapsedNanos / repeatCount;
    }

    private void validateRepeatCount(int repeatCount) {
        if (repeatCount < 1 || repeatCount > 100) {
            throw new IllegalArgumentException("repeatCount must be 1..100.");
        }
    }

}
