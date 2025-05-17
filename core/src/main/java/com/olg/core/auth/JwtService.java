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

/**
 * Service for generating, validating, and parsing JWT (JSON Web Token) tokens.
 */
@Service
public class JwtService {

    /**
     * A record to encapsulate a pair of access and refresh tokens.
     *
     * @param accessToken  the short-lived token used for authentication
     * @param refreshToken the long-lived token used to obtain a new access token
     */
    public record TokenPair(String accessToken, String refreshToken) {
    }

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final Key key;
    private final Long accessExpiration;
    private final Long refreshExpiration;
    private final String issuer;

    /**
     * Constructs the JWT service with the necessary configuration.
     *
     * @param accessExpiration  the expiration duration for access tokens in milliseconds
     * @param refreshExpiration the expiration duration for refresh tokens in milliseconds
     * @param secret            the shared secret used for signing tokens
     * @param issuer            the issuer of the tokens
     */
    public JwtService(@Value("${jwt.access_token.expiration}") long accessExpiration,
                      @Value("${jwt.refresh_token.expiration}") long refreshExpiration,
                      @Value("${jwt.secret}") String secret,
                      @Value("${jwt.issuer}") String issuer) {
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.issuer = issuer;
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generates an access token for the specified user.
     *
     * @param username the user's username
     * @param email    the user's email
     * @param guid      the user's unique guid
     * @param id      the user's unique identifier
     * @return a signed JWT access token
     */
    public String generateAccessToken(String username, String email, String guid, Long id) {
        return generateToken(username, email, guid, id, accessExpiration);
    }

    /**
     * Generates a new access token using a valid refresh token.
     *
     * @param refreshToken the refresh token
     * @return a new access token, or null if the refresh token is invalid
     */
    public String generateAccessToken(String refreshToken) {
        if (validateToken(refreshToken)) {
            UserClaims claims = extractUserClaims(refreshToken);
            return generateAccessToken(
                    claims.username(),
                    claims.email(),
                    claims.guid(),
                    claims.id());
        }

        return null;
    }

//    /**
//     * Generates a refresh token for the specified user.
//     *
//     * @param username the user's username
//     * @param email    the user's email
//     * @param uid      the user's unique identifier
//     * @return a signed JWT refresh token
//     */
//    public String generateRefreshToken(String username, String email, String uid) {
//        return generateToken(username, email, uid, refreshExpiration);
//    }

    /**
     * Generates both an access token and a refresh token for the specified user.
     *
     * @param username the user's username
     * @param email    the user's email
     * @param guid      the user's unique guid
     * @param id      the user's unique identifier
     * @return a {@link TokenPair} containing both tokens
     */
    public TokenPair generateAccessAndRefreshToken(String username, String email, String guid, Long id) {
        return new TokenPair(generateToken(username, email, guid, id, accessExpiration),
                generateToken(username, email, guid, id, refreshExpiration));
    }

    /**
     * Generates a signed JWT token with the given user details and expiration.
     *
     * @param username   the user's username
     * @param email      the user's email
     * @param guid        the user's unique guid
     * @param id        the user's unique identifier
     * @param expiration the expiration duration in milliseconds
     * @return a signed JWT token
     */
    private String generateToken(String username, String email, String guid, Long id, Long expiration) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .claim("guid", guid)
                .claim("id", String.valueOf(id))
                .claim("email", email)
                .signWith(key)
                .compact();
    }

    /**
     * Validates the provided JWT token.
     *
     * @param token the token to validate
     * @return {@code true} if valid, otherwise {@code false}
     */
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
        } catch (Exception e) {
            log.warn("something wrong with JWT: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extracts custom user claims from a valid JWT token.
     *
     * @param token the JWT token
     * @return the extracted {@link UserClaims}
     */
    public UserClaims extractUserClaims(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        log.debug("extractUserClaims-{}", claims);

        return new UserClaims(
                claims.getSubject(),
                (String) claims.get("guid"),
                (String) claims.get("email"),
                Long.valueOf((String) claims.get("id"))
                );
    }
}
