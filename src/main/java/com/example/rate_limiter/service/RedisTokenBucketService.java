package com.example.rate_limiter.service;

import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisTokenBucketService {
    private final StringRedisTemplate redisTemplate;

    public RedisTokenBucketService(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public void createBucket(String clientId, double tokens, long lastRefillTime){
        String key = "rate-limit:" + clientId;

        redisTemplate.opsForHash().put(key, "tokens", String.valueOf(tokens));

        redisTemplate.opsForHash().put(key, "lastRefillTime", String.valueOf(lastRefillTime));
    }

    public String getTokens(String clientId){
        String key = "rate-limit:" + clientId;
        Object value = redisTemplate.opsForHash().get(key, "tokens");
        return value != null ? value.toString() : null;
    }
}
