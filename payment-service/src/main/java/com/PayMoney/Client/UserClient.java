package com.PayMoney.Client;

import com.PayMoney.DTO.UserAuthResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/v1/internal/users/by-email")
    UserAuthResponseDTO getUserByEmail(@RequestParam("email") String email);
}
