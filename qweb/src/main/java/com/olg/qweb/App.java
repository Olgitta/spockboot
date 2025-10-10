package com.olg.qweb;
import com.olg.qweb.configuration.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.olg"},
        exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableJpaRepositories(basePackages = "com.olg.core")
@EntityScan(basePackages = "com.olg.core")
@EnableConfigurationProperties(AppConfig.class)
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
