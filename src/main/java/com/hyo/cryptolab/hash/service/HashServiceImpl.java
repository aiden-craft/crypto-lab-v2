package com.hyo.cryptolab.hash.service;

import com.hyo.cryptolab.common.util.HexUtil;
import com.hyo.cryptolab.common.util.RandomDataUtil;
import com.hyo.cryptolab.hash.dto.AvalancheResponse;
import com.hyo.cryptolab.hash.dto.BulkHashResponse;
import com.hyo.cryptolab.hash.support.HashAlgorithm;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

@Service
public class HashServiceImpl implements HashService {

    @Override
    public String hash(String plainText, String salt, HashAlgorithm algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.getAlgorithm());

            // 교육용 단순 예제:
            // plainText + salt 형태로 결합 후 해시한다.
            String input = plainText + (salt == null ? "" : salt);

            byte[] hashed = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexUtil.bytesToHex(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Hash processing failed.", e);
        }
    }

    @Override
    public AvalancheResponse compareAvalanche(String original, String modified, HashAlgorithm algorithm) {
        String originalHash = hash(original, null, algorithm);
        String modifiedHash = hash(modified, null, algorithm);

        int diffCount = 0;
        int differentBits = 0;
        for (int i = 0; i < Math.min(originalHash.length(), modifiedHash.length()); i++) {
            if (originalHash.charAt(i) != modifiedHash.charAt(i)) {
                diffCount++;
            }
            int a = Character.digit(originalHash.charAt(i), 16);
            int b = Character.digit(modifiedHash.charAt(i), 16);
            differentBits += Integer.bitCount(a ^ b);
        }

        return new AvalancheResponse(
                original,
                modified,
                originalHash,
                modifiedHash,
                diffCount,
                originalHash.length(),
                differentBits,
                originalHash.length() * 4
        );
    }

    @Override
    public BulkHashResponse bulkHash(int count, int length, HashAlgorithm algorithm, boolean useSalt) {
        if (count < 1 || count > 50_000 || length < 1 || length > 64) {
            throw new IllegalArgumentException("count must be 1..50000 and length must be 1..64.");
        }
        Set<String> hashes = new HashSet<>();
        long start = System.nanoTime();

        for (int i = 0; i < count; i++) {
            String plain = RandomDataUtil.randomString(length);
            String salt = useSalt ? RandomDataUtil.randomString(8) : null;
            hashes.add(hash(plain, salt, algorithm));
        }

        long elapsed = (System.nanoTime() - start) / 1_000_000;
        return new BulkHashResponse(count, hashes.size(), elapsed);
    }
}
