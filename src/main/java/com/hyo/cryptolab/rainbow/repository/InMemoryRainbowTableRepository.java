package com.hyo.cryptolab.rainbow.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRainbowTableRepository {

    private final Map<String, String> hashToPlainMap = new ConcurrentHashMap<>();

    public void put(String hash, String plainText) {
        hashToPlainMap.put(hash, plainText);
    }

    public String get(String hash) {
        return hashToPlainMap.get(hash);
    }

    public int size() {
        return hashToPlainMap.size();
    }

    public void clear() {
        hashToPlainMap.clear();
    }

    public Map<String, String> findAll() {
        return hashToPlainMap;
    }
}

