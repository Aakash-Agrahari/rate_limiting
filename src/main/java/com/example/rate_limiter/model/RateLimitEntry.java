package com.example.rate_limiter.model;

public class RateLimitEntry {

    private int requestCount;
    private long windowStart;

    public RateLimitEntry(long windowStart){
        this.windowStart = windowStart;
        this.requestCount = 0;
    }

    
}
