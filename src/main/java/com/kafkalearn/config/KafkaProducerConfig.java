package com.kafkalearn.config;

import com.kafkalearn.domain.LibraryEvent;
import com.kafkalearn.kafka.serialization.LibraryEventSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("local")
public class KafkaProducerConfig {


    private final String bootstrapAddress;

    public KafkaProducerConfig(@Value("${spring.kafka.producer.bootstrap-servers}") final String bootstrapAddress) {
        this.bootstrapAddress = bootstrapAddress;
    }

    @Bean
    public ProducerFactory<Integer, LibraryEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                IntegerSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                LibraryEventSerializer.class);
        configProps.put(
                ProducerConfig.ACKS_CONFIG,
                "all");
        configProps.put(
                ProducerConfig.RETRIES_CONFIG,
                10);
        configProps.put(
                ProducerConfig.RETRY_BACKOFF_MS_CONFIG,
                1000);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<Integer, LibraryEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
