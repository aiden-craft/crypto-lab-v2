package com.hyo.cryptolab.rainbow.service;

public interface RainbowTableService {
    int generateFromRandom(int count, int length);
    int generateFromCommonPasswords();
    String lookup(String hash);
    int size();
    void clear();
}
