package com.PayMoney.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogEventDTO {
    private Long userId;
    private String action;
    private String description;
    private LocalDateTime timestamp;
}
