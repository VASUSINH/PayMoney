package com.PayMoney.Config;

import com.PayMoney.Service.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    //Password Hashing
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
        @Bean
        public SecurityFilterChain securityFilterChain(
                HttpSecurity http) throws Exception {

            http
                    .csrf(csrf -> csrf.disable())
                    .addFilterBefore(jwtAuthenticationFilter,
                            UsernamePasswordAuthenticationFilter.class)
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/addusers","/api/login").permitAll()
                            .anyRequest().authenticated()
                    );

            return http.build();
        }
    }

    

