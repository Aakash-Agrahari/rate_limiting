package com.example.rate_limiter.limiter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class RedisTokenBucketRateLimiter implements RateLimiter {
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> tokenBucketScript;
    private final int capacity;
    private final double refillRate;

    public RedisTokenBucketRateLimiter(
            RedisTemplate<String, String> redisTemplate,
            DefaultRedisScript<Long> tokenBucketScript,
            @Value("${rate-limit.capacity}") int capacity,
            @Value("${rate-limit.refill-rate}") double refillRate
    ){
        this.redisTemplate = redisTemplate;
        this.tokenBucketScript = tokenBucketScript;
        this.capacity = capacity;
        this.refillRate = refillRate;
    }

    @Override
    public boolean allowRequest(String clientId) {
        String key = "rate-limit:" + clientId;
        long currentTime = System.currentTimeMillis();
        Long result = redisTemplate.execute(
                tokenBucketScript,
                Collections.singletonList(key),
                String.valueOf(refillRate),
                String.valueOf(currentTime)
        );
        return result != null && result == 1;
    }
}
