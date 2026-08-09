package com.example.rate_limiter.controller;


import com.example.rate_limiter.limiter.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class TestController {
    private final RateLimiter rateLimiter;

    public TestController(RateLimiter rateLimiter){
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/api/test")
    public ResponseEntity<String> test(@RequestHeader("X-Client-Id") String clientId){
        boolean allowed = rateLimiter.allowRequest(clientId);
        
        if(!allowed){
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }
        return ResponseEntity.ok("Request allowed");
    }
}
