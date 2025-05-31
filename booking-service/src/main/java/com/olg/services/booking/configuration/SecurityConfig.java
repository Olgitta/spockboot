package com.olg.services.booking.configuration;

import com.olg.core.filters.LoggingFilter;
import com.olg.core.filters.RequestResponseLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final AppConfig appConfig;

    public SecurityConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> {
                })
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Allow from same origin (localhost)
                )
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(appConfig.getPublicPaths().toArray(new String[0])).permitAll()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(new LoggingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new RequestResponseLoggingFilter(), LoggingFilter.class)
                .build();
    }
}


