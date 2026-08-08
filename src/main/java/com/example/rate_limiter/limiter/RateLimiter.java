package com.example.rate_limiter.limiter;

public interface RateLimiter {
    boolean allowRequest(String clientId);
}
