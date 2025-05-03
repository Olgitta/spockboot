package com.olg.qweb.configuration;

import com.olg.core.auth.JwtService;
import com.olg.core.filters.JwtAuthenticationFilter;
import com.olg.core.filters.LoggingFilter;
import com.olg.core.filters.RequestResponseLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
//@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Allow from same origin (localhost)
                )
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, "/api/auth/**"),
                                antMatcher(HttpMethod.POST, "/api/register")).permitAll()
                        .anyRequest().authenticated()
                )
                // Best practice for JWT Because:
                //
                //It ensures your JwtAuthenticationFilter runs before Spring's default login/authentication logic.
                //
                //You want the SecurityContextHolder populated before any other filter that checks isAuthenticated().
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new LoggingFilter(), JwtAuthenticationFilter.class)
                .addFilterAfter(new RequestResponseLoggingFilter(), LoggingFilter.class)
                .build();
    }
}


