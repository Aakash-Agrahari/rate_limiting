package com.example.rate_limiter.limiter;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;


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
