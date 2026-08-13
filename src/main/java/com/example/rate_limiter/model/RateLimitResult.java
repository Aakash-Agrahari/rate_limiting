package com.example.rate_limiter.model;

public record RateLimitResult(
        boolean allowed,
        long limit,
        long remaining,
        long retryAfterSeconds
)
{
}
