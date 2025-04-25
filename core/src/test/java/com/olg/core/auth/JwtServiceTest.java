package com.olg.core.auth;

import com.olg.core.auth.dto.UserClaims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static JwtService jwtService;

    @BeforeAll
    static void setup(){
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String secret = Encoders.BASE64.encode(key.getEncoded());
        jwtService = new JwtService(600000,
                86400000,
                secret,
                "app name");
    }

    @Test
    void generateToken_shouldContainClaims() {
        String token = jwtService.generateAccessToken("alice", "mail", "8687687686");
        UserClaims claims = jwtService.extractUserClaims(token);

        assertEquals("alice", claims.username());
    }

    @Test
    void validateToken_shouldReturnFalse_onInvalidToken() {
        String invalidToken = "invalid.token.value";

        assertFalse(jwtService.validateToken(invalidToken));
    }
}