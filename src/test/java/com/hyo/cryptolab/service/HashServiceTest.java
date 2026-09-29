package com.hyo.cryptolab.service;

import com.hyo.cryptolab.hash.dto.AvalancheResponse;
import com.hyo.cryptolab.hash.dto.BulkHashResponse;
import com.hyo.cryptolab.hash.service.HashService;
import com.hyo.cryptolab.hash.service.HashServiceImpl;
import com.hyo.cryptolab.hash.support.HashAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HashServiceTest {

    private HashService hashService;

    @BeforeEach
    void setUp() {
        hashService = new HashServiceImpl();
    }

    @Test
    void shouldGenerateSameHashForSameInput() {
        String hash1 = hashService.hash("password123", null, HashAlgorithm.SHA_256);
        String hash2 = hashService.hash("password123", null, HashAlgorithm.SHA_256);

        Assertions.assertEquals(hash1, hash2);
    }

    @Test
    void shouldGenerateDifferentHashWhenSaltChanges() {
        String hash1 = hashService.hash("password123", "salt1", HashAlgorithm.SHA_256);
        String hash2 = hashService.hash("password123", "salt2", HashAlgorithm.SHA_256);

        Assertions.assertNotEquals(hash1, hash2);
    }

    @Test
    void shouldShowAvalancheEffect() {
        AvalancheResponse response = hashService.compareAvalanche("hello123", "hello124", HashAlgorithm.SHA_256);

        Assertions.assertTrue(response.getDifferentHexChars() > 0);
        Assertions.assertEquals(256, response.getTotalBits());
        Assertions.assertTrue(response.getDifferentBits() > 0);
        Assertions.assertTrue(response.getDifferentBits() <= response.getTotalBits());
        Assertions.assertNotEquals(response.getOriginalHash(), response.getModifiedHash());
    }

    @Test
    void identicalInputHasNoChangedBits() {
        AvalancheResponse response = hashService.compareAvalanche("hello123", "hello123", HashAlgorithm.SHA_512);

        Assertions.assertEquals(512, response.getTotalBits());
        Assertions.assertEquals(0, response.getDifferentBits());
        Assertions.assertEquals(0, response.getDifferentHexChars());
    }

    @Test
    void shouldGenerateUniqueBulkHashes() {
        BulkHashResponse response = hashService.bulkHash(500, 12, HashAlgorithm.SHA_256, false);

        Assertions.assertEquals(500, response.getTotalCount());
        Assertions.assertEquals(500, response.getUniqueHashCount());
    }
}
