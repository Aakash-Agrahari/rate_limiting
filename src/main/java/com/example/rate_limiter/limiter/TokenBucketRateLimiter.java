package com.example.rate_limiter.limiter;

import com.example.rate_limiter.model.TokenBucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


//Token Bucket Rate Limiting works like a bucket that can hold a fixed number of tokens. Each incoming request needs to
// take one token from the bucket before it is allowed to proceed. Tokens are added back into the bucket at a fixed rate
// over time, up to the bucket’s maximum capacity. If a request arrives and a token is available, the request is
// accepted and one token is removed; if the bucket is empty, the request is rejected or delayed until a token becomes
// available. The key advantage is that it allows short bursts of requests when tokens have accumulated, while still
// controlling the average request rate over time.


@Component
@Primary
public class TokenBucketRateLimiter implements RateLimiter {
    private final int CAPACITY;
    private final double REFILL_RATE;

    private final ConcurrentHashMap<String, TokenBucket> clients = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(@Value("${rate-limit.capacity}") int CAPACITY, @Value("${rate-limit.refill-rate}") double REFILL_RATE){
        this.CAPACITY = CAPACITY;
        this.REFILL_RATE = REFILL_RATE;
    }

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
        long elapsedTime = currentTime - bucket.getLastRefillTime();
        double tokensToAdd = (elapsedTime / 1000.0) * REFILL_RATE;
        double newTokenCount = Math.min(
                CAPACITY,
                bucket.getTokens() + tokensToAdd
        );

        bucket.setTokens(newTokenCount);
        bucket.setLastRefillTime(currentTime);
    }
}
