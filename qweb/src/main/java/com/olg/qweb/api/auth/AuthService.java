package com.olg.qweb.api.auth;

import com.olg.core.auth.JwtService;
import com.olg.qweb.api.auth.dto.AuthRequest;
import com.olg.qweb.api.auth.dto.AuthResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtService jwtService;

    public AuthService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AuthResponse login(AuthRequest request) {
        // TODO: implemt real values
        String username = "alice";
        String uid = "999";
        String token = jwtService.generateToken(username, request.email(), uid);
        return new AuthResponse(token);
    }

    public AuthResponse register(AuthRequest request) {
        // TODO: Register user and generate JWT
        String token = "fake-jwt-token-for-" + request.email();
        return new AuthResponse(token);
    }
}

