package com.PayMoney.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class userResponseDTO {
    private Long id;
    private String name;
    private String email;
}
