package com.hyo.cryptolab.hash.dto;

public class BulkHashResponse {

    private int totalCount;
    private int uniqueHashCount;
    private long elapsedMillis;

    public BulkHashResponse(int totalCount, int uniqueHashCount, long elapsedMillis) {
        this.totalCount = totalCount;
        this.uniqueHashCount = uniqueHashCount;
        this.elapsedMillis = elapsedMillis;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getUniqueHashCount() {
        return uniqueHashCount;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }
}
