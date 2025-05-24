package com.olg.kafka.producers;

import com.olg.kafka.BaseKafkaMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class GenericKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public GenericKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public <T> void send(String topic, BaseKafkaMessage<T> message) {
        kafkaTemplate.send(topic, message);
    }
}

