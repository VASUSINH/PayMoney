
package com.PayMoney.Config;

import com.PayMoney.Service.JwtAuthenticationFilter;
import com.PayMoney.Service.RateLimitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RateLimitFilter rateLimitFilter(
            StringRedisTemplate redisTemplate) {
        return new RateLimitFilter(redisTemplate);
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter>
    disableRateLimitServletRegistration(RateLimitFilter filter) {

        FilterRegistrationBean<RateLimitFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RateLimitFilter rateLimitFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(
                        rateLimitFilter,
                        JwtAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/register-user",
                                "/api/v1/auth/login",
                                "/api/v1/auth/reactivate",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/add-money.html",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()
                        .requestMatchers(
                                "/api/payments/**",
                                "/api/my-wallet/**",
                                "/api/wallet/**"
                        ).hasRole("USER")
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
