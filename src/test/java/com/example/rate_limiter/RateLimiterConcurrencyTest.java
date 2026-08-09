package com.example.rate_limiter;

import com.example.rate_limiter.limiter.FixedWindowRateLimiter;
import com.example.rate_limiter.limiter.RateLimiter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimiterConcurrencyTest {

    @Test
    void shouldAllowOnlyFiveConcurrentRequests() throws Exception {

        RateLimiter rateLimiter = new FixedWindowRateLimiter();

        int numberOfRequests = 100;

        ExecutorService executorService =
                Executors.newFixedThreadPool(20);

        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {

            tasks.add(() ->
                    rateLimiter.allowRequest("Aakash")
            );
        }

        List<Future<Boolean>> results =
                executorService.invokeAll(tasks);

        int allowedRequests = 0;

        for (Future<Boolean> result : results) {

            if (result.get()) {
                allowedRequests++;
            }
        }

        executorService.shutdown();

        System.out.println(
                "Allowed requests = " + allowedRequests
        );

        assertEquals(5, allowedRequests);
    }
}