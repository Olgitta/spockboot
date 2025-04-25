package com.olg.qweb.api.auth;

import com.olg.core.auth.JwtService;
import com.olg.qweb.api.auth.dto.AuthRequest;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @Test
    void login_shouldReturnAuthResponseWithToken() {
        // Arrange
        JwtService mockJwtService = mock(JwtService.class);
        AuthService authService = new AuthService(mockJwtService);
        String expectedToken = "mocked-token";
        String username = "alice";
        String email = "alice@example.com";
        String uid = "999";

        AuthRequest request = new AuthRequest(email, "123");

        // Mock the behavior of jwtService
        when(mockJwtService.generateAccessToken(username, email, uid)).thenReturn(expectedToken);

        // Act
        AuthService.AuthTokens response = authService.login(request.email(), request.password());

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.accessToken());

        // Verify that jwtService.generateToken() was called once with the correct username
        verify(mockJwtService).generateAccessToken(username, email, uid);
    }
}
