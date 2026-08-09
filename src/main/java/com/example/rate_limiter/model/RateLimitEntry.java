package com.example.rate_limiter.model;

public class RateLimitEntry {

    private int requestCount;
    private long windowStart;

    public RateLimitEntry(long windowStart){
        this.windowStart = windowStart;
        this.requestCount = 0;
    }

    public int getRequestCount(){
        return requestCount;
    }

    public void setRequestCount(int requestCount){
        this.requestCount = requestCount;
    }

    public long getWindowStart(){
        return windowStart;
    }
    public void setWindowStart(long windowStart){
        this.windowStart = windowStart;
    }
}
