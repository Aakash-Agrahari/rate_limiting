package com.example.rate_limiter;

import com.example.rate_limiter.limiter.RateLimiter;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RedisRateLimiterConcurrencyTest {
    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanBucket(){
        redisTemplate.delete(
                "rate-limit:Aakash"
        );
    }

    @Test
    void shouldAllowOnlyFiveConcurrentRequests() throws Exception{
        int numberOfRequests = 100;

        ExecutorService executor = Executors.newFixedThreadPool(20);

        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<Boolean>> features = new ArrayList<>();

        for(int i = 0; i < numberOfRequests; i++){
            features.add(
                    executor.submit(() -> {
                        startLatch.await();
                        return rateLimiter.allowRequest("Aakash");
                    })
            );
        }

        startLatch.countDown();
        int allowed = 0;
        for(Future<Boolean> future : features){
            if(future.get()){
                allowed++;
            }
        }
        executor.shutdown();
        System.out.println("Allowed requests: "+ allowed);
        assertEquals(5, allowed);
    }
}
