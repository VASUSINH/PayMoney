package com.PayMoney.Mapper;

import com.PayMoney.DTO.walletResponseDTO;
import com.PayMoney.Entity.walletEntity;
import org.springframework.stereotype.Component;

@Component
public class walletMapper {

    //This Function Converts Received Wallet Entity Object From DB to Wallet Response DTO.
    public walletResponseDTO toDTO(walletEntity wallet) {
        return new walletResponseDTO(
                wallet.getWalletId(),
                wallet.getBalance(),
                wallet.getUser().getUserId()
        );
    }
}
