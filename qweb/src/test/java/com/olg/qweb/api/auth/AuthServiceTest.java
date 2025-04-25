package com.olg.qweb.api.auth;

import com.olg.core.auth.JwtService;
import com.olg.qweb.api.auth.dto.AuthRequest;
import com.olg.qweb.api.auth.dto.AuthResponse;
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
        when(mockJwtService.generateToken(username, email, uid)).thenReturn(expectedToken);

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.token());

        // Verify that jwtService.generateToken() was called once with the correct username
        verify(mockJwtService).generateToken(username, email, uid);
    }
}
