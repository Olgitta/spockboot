package com.olg.qweb.api.auth;

import com.olg.core.auth.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final JwtService jwtService;

    public AuthService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public record AuthTokens (String accessToken, String refereshToken) {}

    public AuthTokens login(String email, String password) {

        log.info("User login {}", email);
        // TODO: implement real values
        String username = "alice";
        String uid = "999";
        String token = jwtService.generateAccessToken(username, email, uid);
        String refreshToken = jwtService.generateRefreshToken(username, email, uid);
        return new AuthTokens(token, refreshToken);
    }

    public AuthTokens refresh(String refreshToken) {
        String token = jwtService.generateAccessToken(refreshToken);
        return new AuthTokens(token, null);
    }
}

