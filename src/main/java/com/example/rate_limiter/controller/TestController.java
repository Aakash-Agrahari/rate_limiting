package com.example.rate_limiter.controller;

import com.example.rate_limiter.limiter.RateLimiter;
import com.example.rate_limiter.model.RateLimitResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final RateLimiter rateLimiter;

    public TestController(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/api/test")
    public ResponseEntity<String> test(
            @RequestHeader("X-Client-Id") String clientId) {

        RateLimitResult result =
                rateLimiter.allowRequest(clientId);

        if (!result.allowed()) {

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(
                            "X-RateLimit-Limit",
                            String.valueOf(result.limit())
                    )
                    .header(
                            "X-RateLimit-Remaining",
                            String.valueOf(result.remaining())
                    )
                    .header(
                            "Retry-After",
                            String.valueOf(
                                    result.retryAfterSeconds()
                            )
                    )
                    .body("Rate limit exceeded");
        }

        return ResponseEntity
                .ok()
                .header(
                        "X-RateLimit-Limit",
                        String.valueOf(result.limit())
                )
                .header(
                        "X-RateLimit-Remaining",
                        String.valueOf(result.remaining())
                )
                .body("Request allowed");
    }
}