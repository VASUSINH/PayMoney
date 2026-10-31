package com.PayMoney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableFeignClients
public class PaymentServiceApplication {


	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}
}
