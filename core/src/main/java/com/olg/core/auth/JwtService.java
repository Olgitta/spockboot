package com.olg.core.auth;

import com.olg.core.auth.dto.UserClaims;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final Key key;// = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final Long accessExpiration;
    private final Long refreshExpiration;
    private final String issuer;

    public JwtService(@Value("${jwt.access_token.expiration}") long accessExpiration,
                      @Value("${jwt.refresh_token.expiration}") long refreshExpiration,
                      @Value("${jwt.secret}") String secret,
                      @Value("${jwt.issuer}") String issuer) {
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.issuer = issuer;
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(String username, String email, String uid) {
        return generateToken(username, email, uid, accessExpiration);
    }

    public String generateAccessToken(String refreshToken) {
        if (validateToken(refreshToken)) {
            UserClaims claims = extractUserClaims(refreshToken);
            return generateAccessToken(claims.username(),
                    claims.email(),
                    claims.uid());
        }

        return null;
    }

    public String generateRefreshToken(String username, String email, String uid) {
        return generateToken(username, email, uid, refreshExpiration);
    }

    private String generateToken(String username, String email, String uid, Long expiration) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .claim("uid", uid)
                .claim("email", email)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("Invalid signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Empty or null JWT: {}", e.getMessage());
        }
        return false;
    }

    public UserClaims extractUserClaims(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        log.debug("extractUserClaims-{}", claims);

        return new UserClaims(
                claims.getSubject(),
                (String) claims.get("uid"),
                (String) claims.get("email"));
    }

}

