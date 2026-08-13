package com.example.rate_limiter.limiter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Primary
public class RedisTokenBucketRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> tokenBucketScript;

    private final int capacity;
    private final double refillRate;
    private final long bucketTtl;

    public RedisTokenBucketRateLimiter(
            StringRedisTemplate redisTemplate,
            DefaultRedisScript<Long> tokenBucketScript,
            @Value("${rate-limit.capacity}") int capacity,
            @Value("${rate-limit.refill-rate}") double refillRate,
            @Value("${rate-limit.bucket-ttl}") long bucketTtl) {

        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = tokenBucketScript;
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.bucketTtl = bucketTtl;
    }

    @Override
    public boolean allowRequest(String clientId) {

        String key = "rate-limit:" + clientId;

        long currentTime = System.currentTimeMillis();

        Long result = redisTemplate.execute(
                tokenBucketScript,
                Collections.singletonList(key),
                String.valueOf(capacity),
                String.valueOf(refillRate),
                String.valueOf(currentTime),
                String.valueOf(bucketTtl)
        );

        return result != null && result == 1L;
    }
}