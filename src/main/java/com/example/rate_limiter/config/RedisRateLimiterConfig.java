package com.example.rate_limiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisRateLimiterConfig {

    @Bean
    public DefaultRedisScript<String> tokenBucketScript() {

        DefaultRedisScript<String> script =
                new DefaultRedisScript<>();

        script.setLocation(
                new ClassPathResource(
                        "scripts/token_bucket.lua"
                )
        );

        script.setResultType(String.class);

        return script;
    }
}