package com.tradepilot.core.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${tradepilot.kafka.topics.messages-inbound}")
    private String messagesInboundTopic;

    @Value("${tradepilot.kafka.topics.messages-outbound}")
    private String messagesOutboundTopic;

    @Value("${tradepilot.kafka.topics.follow-up-jobs}")
    private String followUpJobsTopic;

    @Value("${tradepilot.kafka.topics.ai-results}")
    private String aiResultsTopic;

    @Bean
    public NewTopic messagesInboundTopic() {
        return TopicBuilder.name(messagesInboundTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic messagesOutboundTopic() {
        return TopicBuilder.name(messagesOutboundTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic followUpJobsTopic() {
        return TopicBuilder.name(followUpJobsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic aiResultsTopic() {
        return TopicBuilder.name(aiResultsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
