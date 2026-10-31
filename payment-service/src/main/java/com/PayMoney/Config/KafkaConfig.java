package com.PayMoney.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic auditLogTopic(
            @Value("${app.kafka.topics.audit-log}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
