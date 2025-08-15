package com.olg.services.booking;

import com.olg.services.booking.configuration.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.olg"},
        exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableJpaRepositories(basePackages = "com.olg.postgressql")
@EntityScan(basePackages = "com.olg.postgressql")
@EnableConfigurationProperties(AppConfig.class)
public class App {
    public static void main(String[] args) {

        try {
            ApplicationContext context = SpringApplication.run(App.class, args);
        } catch (Exception e) {
            System.out.println("App startup failure!" + e.getMessage());
        }
    }
}