package com.tradepilot.core.config;

import com.tradepilot.core.webhook.dto.AiResultEvent;
import com.tradepilot.core.webhook.dto.OutboundMessageEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, AiResultEvent> aiResultConsumerFactory() {
        JsonDeserializer<AiResultEvent> deserializer = new JsonDeserializer<>(AiResultEvent.class);
        deserializer.addTrustedPackages("com.tradepilot.core.*");
        // Producer does not emit type headers (spring.json.add.type.headers: false)
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
                ),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AiResultEvent> aiResultListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AiResultEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(aiResultConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, OutboundMessageEvent> outboundConsumerFactory() {
        JsonDeserializer<OutboundMessageEvent> deserializer = new JsonDeserializer<>(OutboundMessageEvent.class);
        deserializer.addTrustedPackages("com.tradepilot.core.*");
        deserializer.setUseTypeHeaders(false);

        return new DefaultKafkaConsumerFactory<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
                ),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OutboundMessageEvent> outboundListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OutboundMessageEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(outboundConsumerFactory());
        return factory;
    }
}