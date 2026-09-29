package com.hyo.cryptolab.hash.service;

import com.hyo.cryptolab.hash.dto.AvalancheResponse;
import com.hyo.cryptolab.hash.dto.BulkHashResponse;
import com.hyo.cryptolab.hash.support.HashAlgorithm;

public interface HashService {
    String hash(String plainText, String salt, HashAlgorithm algorithm);
    AvalancheResponse compareAvalanche(String original, String modified, HashAlgorithm algorithm);
    BulkHashResponse bulkHash(int count, int length, HashAlgorithm algorithm, boolean useSalt);
}
