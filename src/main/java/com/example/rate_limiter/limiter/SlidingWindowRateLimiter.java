package com.example.rate_limiter.limiter;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;


//Sliding Window Rate Limiting is a technique that controls how many requests a user can make within a moving time
// period. Instead of dividing time into fixed blocks, it continuously looks at the most recent time window.
// For example, if the limit is 5 requests per 1 minute, the system checks the requests made during the last 60 seconds
// whenever a new request arrives. If there are fewer than 5 requests, the new request is allowed; if there are already
// 5, it is rejected until some older requests fall outside the window. This makes rate limiting more smooth and
// accurate compared to the Fixed Window approach.

@Component
public class SlidingWindowRateLimiter implements RateLimiter {
    private static final int LIMIT = 5;
    private static final long WINDOW_SIZE_MILLIS = 10_000;

    private final ConcurrentHashMap<String, Deque<Long>> clients = new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String clientId) {
        long currentTime = System.currentTimeMillis();
        Deque<Long>  timestamps = clients.computeIfAbsent(
                clientId,
                id -> new ArrayDeque<>()
        );

        synchronized (timestamps){
            long windowStart = currentTime - WINDOW_SIZE_MILLIS;

            while(!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart){
                timestamps.pollFirst();
            }
            if(timestamps.size() >= LIMIT){
                return false;
            }
            timestamps.addLast(currentTime);
            return true;
        }
    }
}
