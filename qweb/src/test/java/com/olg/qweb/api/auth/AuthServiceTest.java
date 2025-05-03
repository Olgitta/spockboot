package com.olg.qweb.api.auth;

import com.github.javafaker.Faker;
import com.olg.core.auth.JwtService;
import com.olg.core.utils.PasswordEncoder;
import com.olg.mysql.users.User;
import com.olg.mysql.users.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthServiceTest {

    @Test
    @DisplayName("Login Should Return Auth Response With Token")
    void login_shouldReturnAuthResponseWithToken() throws Exception {
        Faker faker = new Faker();
        JwtService mockJwtService = mock(JwtService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new PasswordEncoder();

        String expectedToken = "mocked-token";
        String password = "1234567";
        User user = new User();
        user.setName("alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setGuid(UUID.randomUUID());

        AuthService authService = new AuthService(mockJwtService,
                userRepository,
                passwordEncoder);

        when(mockJwtService.generateAccessToken(user.getName(), user.getEmail(), user.getGuid().toString())).thenReturn(expectedToken);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        AuthService.AuthTokens response = authService.login(user.getEmail(), password);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.accessToken());
    }
}
