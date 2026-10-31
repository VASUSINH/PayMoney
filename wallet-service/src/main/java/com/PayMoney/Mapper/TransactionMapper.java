package com.PayMoney.Mapper;

import com.PayMoney.DTO.TransferResponseDTO;
import com.PayMoney.Entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransferResponseDTO toDTO(TransactionEntity transaction) {

        return new TransferResponseDTO(
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