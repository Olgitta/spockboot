package com.olg.core.auth;

import com.olg.core.auth.dto.UserClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final Long expirationTime;
    private final String issuer;

    public JwtService(@Value("${jwt.expiration-time-ms}") long expirationTime,
                      @Value("${jwt.issuer}") String issuer) {
        this.expirationTime = expirationTime;
        this.issuer = issuer;
    }

    public String generateToken(String username, String email, String uid) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .claim("uid", uid)
                .claim("email", email)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        System.out.println("claims|" + claims);
        return claims.getSubject();
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

