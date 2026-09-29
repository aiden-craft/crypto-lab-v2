package com.hyo.cryptolab.common.util;

public final class TimeMeasureUtil {

    private TimeMeasureUtil() {
    }

    public static long measureMillis(Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        long end = System.nanoTime();
        return (end - start) / 1_000_000;
    }
}
