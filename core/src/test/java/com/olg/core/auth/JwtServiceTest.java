package com.olg.core.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(3600000, "app name");

    @Test
    void generateToken_shouldContainUsername() {
        String token = jwtService.generateToken("alice", "mail", "8687687686");
        String username = jwtService.extractUsername(token);

        assertEquals("alice", username);
    }

    @Test
    void extractUsername_shouldThrow_onInvalidToken() {
        String invalidToken = "invalid.token.value";

        assertThrows(Exception.class, () -> jwtService.extractUsername(invalidToken));
    }
}