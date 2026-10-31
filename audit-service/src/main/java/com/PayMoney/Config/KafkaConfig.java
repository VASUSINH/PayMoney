package com.PayMoney.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;


@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic auditLogTopic(
            @Value("${app.kafka.topics.audit-log}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    // If a listener fails, retry 5 times (1 second apart). After that the
    // message is moved to "<topic>.DLT" (dead letter topic) instead of being
    // lost. The retries also cover the case where "user-updated" arrives
    // before the wallet has been created from "user-registered".
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(record.topic() + ".DLT", -1));

        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 5));
    }
}
