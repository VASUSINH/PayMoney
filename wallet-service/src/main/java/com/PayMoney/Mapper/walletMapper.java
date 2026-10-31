
package com.PayMoney.Mapper;

import com.PayMoney.DTO.WalletResponseDTO;
import com.PayMoney.Entity.WalletEntity;
import org.springframework.stereotype.Component;

@Component
public class walletMapper {

    public WalletResponseDTO toDTO(WalletEntity wallet) {
        return new WalletResponseDTO(
                wallet.getWalletId(),
                wallet.getBalance(),
                wallet.getUserId(),
                wallet.getName()
        );
    }
}
