package com.example.rate_limiter.limiter;

import com.example.rate_limiter.model.RateLimitEntry;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;


//Fixed Window Rate Limiting divides time into fixed intervals, such as 1 minute, and allows a maximum number
// of requests within each interval. For example, if the limit is 5 requests per minute, the system counts
// requests from 10:00:00 to 10:00:59; once the user reaches 5 requests, further requests are rejected until
// the next window starts at 10:01:00, when the counter resets to 0.
/*
@Component
public class FixedWindowRateLimiter implements RateLimiter{
    private final ConcurrentHashMap<String, RateLimitEntry> clients =
            new ConcurrentHashMap<>();

    private static final int limit = 5;
    private static final long windowSizeMillis = 10_000;

    //this is to provide the rate limiting value by ourself
    //public FixedWindowRateLimiter(int limit, long windowSizeMillis) {
    //    this.limit = limit;
    //    this.windowSizeMillis = windowSizeMillis;
    //}

    @Override
    public boolean allowRequest(String clientId) {
        long currentTime = System.currentTimeMillis();
        RateLimitEntry entry = clients.computeIfAbsent(
                clientId,
                id -> new RateLimitEntry(currentTime)
        );

        synchronized (entry){
            if (currentTime - entry.getWindowStart() >= windowSizeMillis){
                entry.setWindowStart(currentTime);
                entry.setRequestCount(0);
            }

            if(entry.getRequestCount() >= limit){
                return false;
            }

            entry.setRequestCount(
                    entry.getRequestCount() + 1
            );
            return true;
        }
    }
}*/