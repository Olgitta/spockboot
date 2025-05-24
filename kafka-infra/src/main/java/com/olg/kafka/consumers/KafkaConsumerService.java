package com.olg.kafka.consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumerService {

    @KafkaListener(topics = "my-topic", groupId = "my-consumer-group")
    public void listen(ConsumerRecord<String, String> record) {
        System.out.printf("Consumed message: key=%s, value=%s%n", record.key(), record.value());
    }
}

