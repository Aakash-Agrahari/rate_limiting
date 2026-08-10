package com.example.rate_limiter.limiter;

import com.example.rate_limiter.model.TokenBucket;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


@Component
public class TokenBucketRateLimiter implements RateLimiter {
    private static final int CAPACITY = 5;
    private static final double REFILL_RATE = 1.0 / 2.0;

    private final ConcurrentHashMap<String, TokenBucket> clients = new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String clientId) {
        long currentTime = System.currentTimeMillis();

        TokenBucket bucket = clients.computeIfAbsent(
                clientId,
                id -> new TokenBucket(
                        CAPACITY,
                        currentTime
                )
        );

        synchronized (bucket){
            refillTokens(bucket, currentTime);

            if(bucket.getTokens() < 1){
                return false;
            }
            bucket.setTokens(
                    bucket.getTokens() - 1
            );
            return true;
        }
    }
    private void refillTokens(TokenBucket bucket, long currentTime){
        long elapsedTime = bucket.getLastRefillTime();
        double tokensToAdd = (elapsedTime / 1000.0) * REFILL_RATE;
        double newTokenCount = Math.min(
                CAPACITY,
                bucket.getTokens() + tokensToAdd
        );

        bucket.setTokens(newTokenCount);
        bucket.setLastRefillTime(currentTime);
    }
}
