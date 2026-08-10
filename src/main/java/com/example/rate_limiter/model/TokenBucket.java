package com.example.rate_limiter.model;

public class TokenBucket {
    private double tokens;
    private long lastRefillTime;

    public TokenBucket(double tokens, long lastRefillTime){
        this.tokens = tokens;
        this.lastRefillTime = lastRefillTime;
    }
    public double getTokens(){
        return tokens;
    }
    public void setTokens(double tokens){
        this.tokens = tokens;
    }
    public long getLastRefillTime(){
        return lastRefillTime;
    }
    public void setLastRefillTime(long lastRefillTime){
        this.lastRefillTime = lastRefillTime;
    }
}
