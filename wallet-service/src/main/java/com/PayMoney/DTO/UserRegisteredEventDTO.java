package com.PayMoney.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisteredEventDTO {
    private Long userId;
    private String name;
    private String email;
}
