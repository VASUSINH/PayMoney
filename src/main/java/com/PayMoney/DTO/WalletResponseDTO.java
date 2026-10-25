
package com.PayMoney.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WalletResponseDTO {

        private Long walletId;
        private BigDecimal balance;
        private Long userId;
        private String name;
}
