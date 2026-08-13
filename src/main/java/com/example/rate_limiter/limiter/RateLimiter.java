package com.example.rate_limiter.limiter;

import com.example.rate_limiter.model.RateLimitResult;

public interface RateLimiter {
    RateLimitResult allowRequest(String clientId);
}
