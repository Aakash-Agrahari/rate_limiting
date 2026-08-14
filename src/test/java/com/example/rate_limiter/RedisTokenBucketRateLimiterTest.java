package com.example.rate_limiter;


import com.example.rate_limiter.limiter.RateLimiter;
import com.example.rate_limiter.model.RateLimitResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisTokenBucketRateLimiterTest {

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanRedis(){
        redisTemplate.delete("rate-limit: Aakash");
        redisTemplate.delete("rate-limit: Sky");
    }

    @Test
    void shouldAllowFiveInitialRequests(){
        for(int i=1; i<=5; i++){
            RateLimitResult result = rateLimiter.allowRequest("Aakash");
            assertTrue(
                    result.allowed(),
                    "Request " + i + "should be allowed"
            );
        }
    }

    @Test
    void shouldRejectSixthRequest(){
        for(int i=0; i<5; i++){
            rateLimiter.allowRequest("Aakash");
        }
        RateLimitResult result = rateLimiter.allowRequest("Aakash");

        assertFalse(result.allowed());
        assertEquals(5, result.limit());
        assertEquals(0, result.remaining());
        assertTrue(
                result.retryAfterSeconds() > 0
        );
    }
}
