package com.hyo.cryptolab.rainbow.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 실제 유출 패스워드 DB(RockYou, HIBP 등)에서 추출한 패턴 기반으로
 * 취약한 패스워드 100만 건을 생성해 common-passwords.txt 에 덮어씁니다.
 *
 * 실행: main() 직접 실행 (IDE Run 또는 mvn exec:java)
 * 출력: src/main/resources/sample/common-passwords.txt
 */
public class CommonPasswordsGeneratorTest {

    // ---------------------------------------------------------------
    // 시드 — 실제 유출 목록 상위 패턴 기반 취약 패스워드 원형
    // ---------------------------------------------------------------
    private static final List<String> SEEDS = List.of(

        // ── 순수 숫자 시퀀스 (RockYou 1~10위 단골) ──────────────────
        "123456", "1234567", "12345678", "123456789", "1234567890",
        "12345", "1234", "123", "111111", "000000",
        "11111111", "1111", "0000", "12341234", "123123",
        "654321", "112233", "121212", "010203", "102030",
        "1357924", "2580", "1472583690",

        // ── 키보드 워크 패턴 ────────────────────────────────────────
        "qwerty", "qwerty123", "qwer1234", "qwerty1", "qwertyuiop",
        "asdf", "asdfgh", "asdfghjkl", "asdf1234",
        "zxcvbn", "zxcvbnm",
        "1q2w3e", "1q2w3e4r", "1q2w3e4r5t", "q1w2e3r4",
        "!qaz2wsx", "1qaz2wsx", "qazwsx",
        "poiuyt", "mnbvcxz",

        // ── 단어 + 변형 (사전 공격 핵심) ────────────────────────────
        "password", "password1", "password12", "password!",
        "pass", "passwd", "passcode", "passw0rd", "p@ssword",
        "admin", "admin1", "admin123", "admin@123",
        "administrator", "root", "root123",
        "login", "logon", "logout",
        "welcome", "welcome1", "welcome!",
        "test", "test1", "test123", "testing",
        "demo", "demo123", "guest", "guest123",
        "user", "user1", "user123", "username",
        "access", "access1",
        "letmein", "let me in",
        "changeme", "change_me",
        "default", "temp", "temp1234",
        "abc", "abcd", "abcde",
        "abc123", "abcd1234", "abc1234", "abcd123",

        // ── 이름 + 숫자 (유출 DB 상위권) ────────────────────────────
        "michael", "daniel", "jordan", "jessica", "jennifer",
        "ashley", "sarah", "nicole", "amanda", "taylor",
        "james", "john", "david", "chris", "kevin",
        "thomas", "robert", "mark", "jason", "ryan",
        "mike", "alex", "sam", "tom", "jake",
        "kim", "park", "lee", "choi", "jung",

        // ── 감정·일상 단어 ───────────────────────────────────────────
        "iloveyou", "iloveyou1", "loveyou", "love",
        "monkey", "dragon", "shadow", "master",
        "sunshine", "princess", "angel", "baby",
        "soccer", "football", "baseball", "basketball", "tennis",
        "superman", "batman", "ironman", "spiderman",
        "ninja", "hunter", "killer", "winner",
        "hello", "hello1", "hello123",
        "world", "sunshine", "freedom", "liberty",

        // ── 한국 서비스·브랜드 연상 패턴 ────────────────────────────
        "korea", "korean", "seoul", "busan", "incheon",
        "samsung", "lg", "hyundai", "lotte", "sk",
        "naver", "kakao", "kakao123", "naver123",
        "kt", "skt", "lgu",

        // ── 날짜·연도 패턴 ───────────────────────────────────────────
        "011234", "990101", "000101", "010101",
        "jan2024", "jan2025", "dec2024",
        "2024", "2025", "2026", "2023", "2022", "2019",

        // ── 특수문자 혼합 (흔히 복잡도 요건 우회용) ──────────────────
        "P@ssw0rd", "P@ssword1", "Passw0rd!",
        "Qwerty123!", "Admin@123", "Welcome1!",
        "Test@123", "Test1234!",
        "qwer!@#$",

        // ── 반복·단순 패턴 ───────────────────────────────────────────
        "aaaaaa", "aaa111", "aabbcc",
        "abcabc", "password2", "password3"
    );

    // ── 숫자 접미사 (유출 목록에서 빈도 높은 순서) ──────────────────
    private static final int[] NUMBER_SUFFIXES_PRIORITY = {
        1, 2, 3, 12, 21, 123, 321, 1234, 4321, 12345,
        0, 00, 11, 22, 33, 99, 100, 111, 999, 2024, 2025, 2026,
        2023, 2022, 2021, 2020, 2019, 2018, 2010, 2000, 1999
    };

    private static final String[] CHAR_SUFFIXES = {
        "!", "!!", "@", "#", "!@#", "123!", "1!", "1234!", "!!!", "**", "@@"
    };

    private static final String[] CHAR_PREFIXES = {
        "1", "12", "123", "!", "0"
    };

    static final int TARGET = 1_000_000;

    public static void main(String[] args) throws IOException {
        long startedAt = System.nanoTime();
        Set<String> result = new LinkedHashSet<>();

        // 1. 시드 원형 그대로 추가
        for (String seed : SEEDS) {
            add(result, seed);
            if (isFull(result)) {
                break;
            }
        }

        // 2. 숫자 접미사 우선순위 조합
        if (!isFull(result)) {
            for (String seed : SEEDS) {
                for (int n : NUMBER_SUFFIXES_PRIORITY) {
                    add(result, seed + n);
                    if (isFull(result)) {
                        break;
                    }
                }
                if (isFull(result)) {
                    break;
                }
            }
        }

        // 3. 숫자 1~9999 순차 조합 (핵심 볼륨 채우기)
        if (!isFull(result)) {
            outer:
            for (int n = 1; n <= 9999; n++) {
                for (String seed : SEEDS) {
                    add(result, seed + n);
                    if (isFull(result)) {
                        break outer;
                    }
                }
            }
        }

        // 4. 특수문자 접미사
        if (!isFull(result)) {
            for (String seed : SEEDS) {
                for (String s : CHAR_SUFFIXES) {
                    add(result, seed + s);
                    if (isFull(result)) {
                        break;
                    }
                }
                if (isFull(result)) {
                    break;
                }
            }
        }

        // 5. 특수문자 접두사
        if (!isFull(result)) {
            for (String seed : SEEDS) {
                for (String p : CHAR_PREFIXES) {
                    add(result, p + seed);
                    if (isFull(result)) {
                        break;
                    }
                }
                if (isFull(result)) {
                    break;
                }
            }
        }

        // 6. 첫 글자 대문자 변형
        if (!isFull(result)) {
            for (String seed : SEEDS) {
                if (seed.length() > 0 && Character.isLetter(seed.charAt(0))) {
                    String cap = Character.toUpperCase(seed.charAt(0)) + seed.substring(1);
                    add(result, cap);
                    add(result, cap + "1");
                    add(result, cap + "123");
                    add(result, cap + "!");
                    add(result, cap + "@123");
                }
                if (isFull(result)) {
                    break;
                }
            }
        }

        // 7. 두 시드 조합 (seed1 + seed2 형태)
        if (!isFull(result)) {
            String[] shortSeeds = SEEDS.stream()
                .filter(s -> s.length() <= 6)
                .limit(30)
                .toArray(String[]::new);
            outerLoop:
            for (String a : shortSeeds) {
                for (String b : shortSeeds) {
                    if (!a.equals(b)) {
                        add(result, a + b);
                        if (isFull(result)) {
                            break outerLoop;
                        }
                    }
                }
            }
        }

        // 8. 예외 상황 대비: 중복으로 목표 미달 시 기계적 보강
        if (!isFull(result)) {
            fillFallback(result);
        }

        List<String> finalList = new ArrayList<>(result);
        if (finalList.size() > TARGET) {
            finalList = new ArrayList<>(finalList.subList(0, TARGET));
        }

        // 출력 경로 결정 (프로젝트 루트 기준)
        String cwd = System.getProperty("user.dir");
        Path outputPath = Paths.get(cwd, "src/main/resources/sample/common-passwords.txt");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, finalList, StandardCharsets.UTF_8);

        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        System.out.printf("Generated %,d passwords in %,d ms%n", finalList.size(), elapsedMs);
        System.out.println(outputPath.toAbsolutePath());
    }

    private static void add(Set<String> set, String value) {
        if (set.size() < TARGET && value != null && !value.isBlank()) {
            set.add(value);
        }
    }

    private static boolean isFull(Set<String> set) {
        return set.size() >= TARGET;
    }

    private static void fillFallback(Set<String> set) {
        int i = 1;
        while (!isFull(set)) {
            for (String seed : SEEDS) {
                add(set, seed + "_" + i);
                if (isFull(set)) {
                    break;
                }
            }
            i++;
        }
    }
}
