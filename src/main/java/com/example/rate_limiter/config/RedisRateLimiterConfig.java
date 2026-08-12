package com.example.rate_limiter.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisRateLimiterConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template =
                new RedisTemplate<>();

        template.setConnectionFactory(
                connectionFactory
        );

        template.setKeySerializer(
                new StringRedisSerializer()
        );

        template.setValueSerializer(
                new StringRedisSerializer()
        );

        template.setHashKeySerializer(
                new StringRedisSerializer()
        );

        template.setHashValueSerializer(
                new StringRedisSerializer()
        );

        template.afterPropertiesSet();

        return template;
    }


    @Bean
    public DefaultRedisScript<Long> tokenBucketScript(){
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();

        script.setLocation(
                new ClassPathResource(
                        "scripts/token_bucket.lua"
                )
        );
        script.setResultType(Long.class);

        return script;
    }
}
