package com.PayMoney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableCaching
@EnableFeignClients
public class WalletServiceApplication {


	public static void main(String[] args) {
		SpringApplication.run(WalletServiceApplication.class, args);
	}
}
