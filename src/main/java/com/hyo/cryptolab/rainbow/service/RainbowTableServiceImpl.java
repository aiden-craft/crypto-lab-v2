package com.hyo.cryptolab.rainbow.service;

import com.hyo.cryptolab.common.util.RandomDataUtil;
import com.hyo.cryptolab.hash.service.HashService;
import com.hyo.cryptolab.hash.support.HashAlgorithm;
import com.hyo.cryptolab.rainbow.repository.InMemoryRainbowTableRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class RainbowTableServiceImpl implements RainbowTableService {

    private final InMemoryRainbowTableRepository repository;
    private final HashService hashService;

    public RainbowTableServiceImpl(InMemoryRainbowTableRepository repository, HashService hashService) {
        this.repository = repository;
        this.hashService = hashService;
    }

    @Override
    public int generateFromRandom(int count, int length) {
        if (count < 1 || count > 50_000 || length < 1 || length > 64) {
            throw new IllegalArgumentException("count must be 1..50000 and length must be 1..64.");
        }
        repository.clear();
        for (int i = 0; i < count; i++) {
            String plain = RandomDataUtil.randomString(length);
            String hash = hashService.hash(plain, null, HashAlgorithm.SHA_256);
            repository.put(hash, plain);
        }
        return repository.size();
    }

    @Override
    public int generateFromCommonPasswords() {
        repository.clear();

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                new ClassPathResource("sample/common-passwords.txt").getInputStream(),
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                String plain = line.trim();
                if (!plain.isEmpty()) {
                    String hash = hashService.hash(plain, null, HashAlgorithm.SHA_256);
                    repository.put(hash, plain);
                }
            }
            return repository.size();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build rainbow table.", e);
        }
    }

    @Override
    public String lookup(String hash) {
        return repository.get(hash);
    }

    @Override
    public int size() {
        return repository.size();
    }

    @Override
    public void clear() {
        repository.clear();
    }
}
