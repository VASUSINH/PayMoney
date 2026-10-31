package com.PayMoney.Producer;

import com.PayMoney.DTO.UserRegisteredEventDTO;
import com.PayMoney.DTO.UserUpdatedEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class UserEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(UserEventProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.user-registered}")
    private String userRegisteredTopic;

    @Value("${app.kafka.topics.user-updated}")
    private String userUpdatedTopic;

    public void publishUserRegistered(UserRegisteredEventDTO event) {
        sendAfterCommit(userRegisteredTopic,
                String.valueOf(event.getUserId()), event);
    }

    public void publishUserUpdated(UserUpdatedEventDTO event) {
        sendAfterCommit(userUpdatedTopic,
                String.valueOf(event.getUserId()), event);
    }

    // The event is only published once the database transaction has
    // committed, so a rolled back registration never reaches Kafka.
    private void sendAfterCommit(String topic, String key, Object event) {

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            send(topic, key, event);
                        }
                    });
            return;
        }

        send(topic, key, event);
    }

    private void send(String topic, String key, Object event) {
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error("Failed to publish event to topic {}",
                                topic, exception);
                    }
                });
    }
}
