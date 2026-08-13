package com.example.rate_limiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisRateLimiterConfig {

    @Bean
    public DefaultRedisScript<Long> tokenBucketScript() {

        DefaultRedisScript<Long> script =
                new DefaultRedisScript<>();

        script.setLocation(
                new ClassPathResource(
                        "scripts/token_bucket.lua"
                )
        );

        script.setResultType(Long.class);

        return script;
    }
}