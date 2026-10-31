package com.PayMoney.Consumer;

import com.PayMoney.DTO.UserRegisteredEventDTO;
import com.PayMoney.DTO.UserUpdatedEventDTO;
import com.PayMoney.Service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    @Autowired
    private WalletService walletService;

    @KafkaListener(topics = "${app.kafka.topics.user-registered}")
    public void onUserRegistered(UserRegisteredEventDTO event) {
        walletService.createWallet(event);
    }

    @KafkaListener(topics = "${app.kafka.topics.user-updated}")
    public void onUserUpdated(UserUpdatedEventDTO event) {
        walletService.updateWalletName(event);
    }
}
