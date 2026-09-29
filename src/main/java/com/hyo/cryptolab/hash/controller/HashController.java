package com.hyo.cryptolab.hash.controller;

import com.hyo.cryptolab.hash.dto.AvalancheResponse;
import com.hyo.cryptolab.hash.dto.BulkHashResponse;
import com.hyo.cryptolab.hash.dto.HashRequest;
import com.hyo.cryptolab.hash.dto.HashResponse;
import com.hyo.cryptolab.hash.service.HashService;
import com.hyo.cryptolab.hash.support.HashAlgorithm;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hash")
public class HashController {

    private final HashService hashService;

    public HashController(HashService hashService) {
        this.hashService = hashService;
    }

    @PostMapping("/generate")
    public HashResponse generate(@Valid @RequestBody HashRequest request) {
        HashAlgorithm algorithm = HashAlgorithm.valueOf(request.getAlgorithm());
        String hashValue = hashService.hash(request.getPlainText(), request.getSalt(), algorithm);
        return new HashResponse(
                algorithm.name(),
                request.getPlainText(),
                request.getSalt(),
                hashValue
        );
    }

    @GetMapping("/avalanche")
    public AvalancheResponse avalanche(
            @RequestParam String original,
            @RequestParam String modified,
            @RequestParam(defaultValue = "SHA_256") String algorithm
    ) {
        return hashService.compareAvalanche(original, modified, HashAlgorithm.valueOf(algorithm));
    }

    @GetMapping("/bulk")
    public BulkHashResponse bulk(
            @RequestParam(defaultValue = "10000") int count,
            @RequestParam(defaultValue = "12") int length,
            @RequestParam(defaultValue = "SHA_256") String algorithm,
            @RequestParam(defaultValue = "false") boolean useSalt
    ) {
        return hashService.bulkHash(count, length, HashAlgorithm.valueOf(algorithm), useSalt);
    }
}
