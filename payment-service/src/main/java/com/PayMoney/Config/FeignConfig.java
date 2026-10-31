package com.PayMoney.Config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    // Every Feign call to another PayMoney service carries this key.
    // The internal endpoints reject requests that do not have it.
    @Bean
    public RequestInterceptor internalKeyInterceptor(
            @Value("${internal.api.key}") String internalApiKey) {

        return template -> template.header("X-Internal-Key", internalApiKey);
    }
}
