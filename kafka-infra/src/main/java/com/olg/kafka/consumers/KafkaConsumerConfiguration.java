package com.olg.kafka.consumers;

import com.olg.kafka.BaseKafkaMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConsumerConfiguration {
    @Bean
    public ConsumerFactory<String, BaseKafkaMessage<Object>> baseMessageConsumerFactory() {
        JsonDeserializer<BaseKafkaMessage<Object>> deserializer = new JsonDeserializer<>(BaseKafkaMessage.class);
        deserializer.addTrustedPackages("*"); // or specify package

        return new DefaultKafkaConsumerFactory<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
                        ConsumerConfig.GROUP_ID_CONFIG, "generic-consumer",
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class
                ),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean(name = "baseMessageListenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, BaseKafkaMessage<Object>> baseMessageListenerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, BaseKafkaMessage<Object>>();
        factory.setConsumerFactory(baseMessageConsumerFactory());
        return factory;
    }

}
