package com.example.rate_limiter.limiter;

import com.example.rate_limiter.model.RateLimitEntry;

import java.util.concurrent.ConcurrentHashMap;

public class FixedWindowRateLimiter implements RateLimiter{
    private final ConcurrentHashMap<String, RateLimitEntry> clients =
            new ConcurrentHashMap<>();

    private final int limit;
    private final long windowSizeMillis;

    public FixedWindowRateLimiter(int limit, long windowSizeMillis) {
        this.limit = limit;
        this.windowSizeMillis = windowSizeMillis;
    }

    @Override
    public boolean allowRequest(String clientId) {
        return false;
    }
}
