package com.example.rate_limiter.limiter;

import com.example.rate_limiter.model.TokenBucket;

import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter implements RateLimiter {
    private static final int CAPACITY = 5;
    private static final double REFILL_RATE = 1.0 / 2.0;

    private final ConcurrentHashMap<String, TokenBucket> clients = new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String clientId) {
        return false;
    }
}
