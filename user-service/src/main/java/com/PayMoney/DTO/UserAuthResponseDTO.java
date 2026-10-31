package com.PayMoney.DTO;

import com.PayMoney.Entity.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthResponseDTO {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private AccountStatus accountStatus;
}
