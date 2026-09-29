package com.hyo.cryptolab.rainbow.controller;

import com.hyo.cryptolab.rainbow.service.RainbowTableService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rainbow")
public class RainbowTableController {

    private final RainbowTableService rainbowTableService;

    public RainbowTableController(RainbowTableService rainbowTableService) {
        this.rainbowTableService = rainbowTableService;
    }

    @PostMapping("/generate/random")
    public Map<String, Object> generateRandom(
            @RequestParam(defaultValue = "1000") int count,
            @RequestParam(defaultValue = "8") int length
    ) {
        int size = rainbowTableService.generateFromRandom(count, length);
        return Map.of("generatedSize", size);
    }

    @PostMapping("/generate/common")
    public Map<String, Object> generateCommon() {
        int size = rainbowTableService.generateFromCommonPasswords();
        return Map.of("generatedSize", size);
    }

    @GetMapping("/lookup")
    public Map<String, Object> lookup(@RequestParam String hash) {
        String plain = rainbowTableService.lookup(hash);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("hash", hash);
        result.put("found", plain != null);
        result.put("plainText", plain != null ? plain : "");
        result.put("message", plain != null ? "일치하는 원문을 찾았습니다." : "일치하는 원문이 없습니다.");

        return result;
    }

    @DeleteMapping
    public Map<String, Object> clear() {
        rainbowTableService.clear();
        return Map.of("cleared", true);
    }
}
