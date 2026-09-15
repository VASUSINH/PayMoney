package com.PayMoney.Mapper;

import com.PayMoney.DTO.transactionResponseDTO;
import com.PayMoney.Entity.transactionEntity;
import org.springframework.stereotype.Component;

@Component
public class transactionMapper {

    public transactionResponseDTO toDTO(transactionEntity transaction) {

        return new transactionResponseDTO(
                transaction.getTransactionId(),

                transaction.getSenderWallet() != null
                        ? transaction.getSenderWallet().getWalletId()
                        : null,

                transaction.getReceiverWallet() != null
                        ? transaction.getReceiverWallet().getWalletId()
                        : null,

                transaction.getAmount(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}