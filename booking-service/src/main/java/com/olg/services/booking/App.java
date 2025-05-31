package com.olg.services.booking;

import com.olg.kafka.producers.KafkaProducerConfiguration;
import com.olg.services.booking.configuration.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.olg"},
        exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableJpaRepositories(basePackages = "com.olg.mysql")
@EntityScan(basePackages = "com.olg.mysql")
@EnableConfigurationProperties(AppConfig.class)
@Import(KafkaProducerConfiguration.class)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}