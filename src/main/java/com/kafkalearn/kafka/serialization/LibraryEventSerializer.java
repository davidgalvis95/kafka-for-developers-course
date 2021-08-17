package com.kafkalearn.kafka.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkalearn.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class LibraryEventSerializer implements Serializer<LibraryEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String topic, LibraryEvent data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (RuntimeException | JsonProcessingException e) {
            log.error("Something went wrong with th serialization of the library event");
            throw new SerializationException("Error serializing value ", e);
        }
    }

//    @Override
//    public byte[] serialize(String topic, Headers headers, LibraryEvent data) {
//        return Serializer.super.serialize(topic, headers, data);
//    }

    @Override
    public void close() {
        Serializer.super.close();
    }
}
