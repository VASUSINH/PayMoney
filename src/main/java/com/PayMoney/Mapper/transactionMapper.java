package com.PayMoney.Mapper;

import com.PayMoney.DTO.transferResponseDTO;
import com.PayMoney.Entity.transactionEntity;
import org.springframework.stereotype.Component;

@Component
public class transactionMapper {

    public transferResponseDTO toDTO(transactionEntity transaction) {

        return new transferResponseDTO(
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