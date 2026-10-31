package com.PayMoney.Config;

import com.PayMoney.Service.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimitFilter rateLimitFilter(
            StringRedisTemplate redisTemplate) {
        return new RateLimitFilter(redisTemplate);
    }
}
