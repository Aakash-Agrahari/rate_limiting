package com.example.rate_limiter;


import com.example.rate_limiter.limiter.RateLimiter;
import com.example.rate_limiter.model.RateLimitResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisTokenBucketRateLimiterTest {

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanRedis(){
        redisTemplate.delete("rate-limit:Aakash");
        redisTemplate.delete("rate-limit:Sky");
    }

    @Test
    void shouldAllowFiveInitialRequests(){
        for(int i=1; i<=5; i++){
            RateLimitResult result = rateLimiter.allowRequest("Aakash");
            System.out.println(
                    "Request " + i +
                            " -> allowed=" + result.allowed() +
                            ", limit=" + result.limit() +
                            ", remaining=" + result.remaining() +
                            ", retryAfter=" + result.retryAfterSeconds()
            );
            assertTrue(
                    result.allowed(),
                    "Request " + i + " should be allowed"
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

    @Test
    void differentClientsShouldHaveIndependentBuckets(){
        //Exhaust Aakash's bucket
        for(int i=0; i<5; i++){
            rateLimiter.allowRequest("Aakash");
        }
        RateLimitResult aakashResult = rateLimiter.allowRequest("Aakash");

        //Sky should still have a full bucket
        RateLimitResult skyResult = rateLimiter.allowRequest("Sky");
        assertFalse(aakashResult.allowed());
        assertTrue(skyResult.allowed());
        assertEquals(
                4,
                skyResult.remaining()
        );
    }

    @Test
    void shouldHandleConcurrentRequests()
            throws Exception {

        int numberOfRequests = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(20);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        List<Future<RateLimitResult>> futures =
                new ArrayList<>();

        for (int i = 0;
             i < numberOfRequests;
             i++) {

            futures.add(
                    executor.submit(() -> {

                        startLatch.await();

                        return rateLimiter
                                .allowRequest("Aakash");
                    })
            );
        }

        // Release all threads at approximately
        // the same time.
        startLatch.countDown();

        int allowed = 0;
        int rejected = 0;

        for (Future<RateLimitResult> future : futures) {

            RateLimitResult result =
                    future.get();

            if (result.allowed()) {
                allowed++;
            } else {
                rejected++;
            }
        }

        executor.shutdown();

        System.out.println(
                "Allowed: " + allowed
        );

        System.out.println(
                "Rejected: " + rejected
        );

        assertEquals(5, allowed);

        assertEquals(
                95,
                rejected
        );
    }

    @Test
    void shouldAllowRequestAfterTokenRefill()
            throws InterruptedException {

        // Consume all 5 tokens
        for (int i = 0; i < 5; i++) {
            rateLimiter.allowRequest("Aakash");
        }

        // Bucket should now be empty
        RateLimitResult rejected =
                rateLimiter.allowRequest("Aakash");

        System.out.println(
                "Immediately after exhaustion -> allowed="
                        + rejected.allowed()
                        + ", remaining="
                        + rejected.remaining()
                        + ", retryAfter="
                        + rejected.retryAfterSeconds()
        );

        assertFalse(rejected.allowed());

        // Wait approximately 2 seconds.
        // Refill rate = 0.5 token/second
        // Therefore 1 token is generated every 2 seconds.
        Thread.sleep(2100);

        RateLimitResult allowed =
                rateLimiter.allowRequest("Aakash");

        System.out.println(
                "After refill -> allowed="
                        + allowed.allowed()
                        + ", remaining="
                        + allowed.remaining()
                        + ", retryAfter="
                        + allowed.retryAfterSeconds()
        );

        assertTrue(allowed.allowed());
    }

}
