package com.olg.core.filters;

import com.olg.core.auth.CustomAuthenticationToken;
import com.olg.core.auth.JwtService;
import com.olg.core.auth.dto.UserClaims;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

//@WebFilter
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // gets the token from the Authorization header.
        // If it's a valid token, it sets the authentication in the SecurityContextHolder.

        // Get the Authorization header from the request
        String authorizationHeader = request.getHeader("Authorization");

        // If there is a token in the Authorization header, validate it
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            try {
                if (jwtService.validateToken(token)) {
                    UserClaims userClaims = jwtService.extractUserClaims(token);
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    Authentication authentication =
                            new CustomAuthenticationToken(userClaims);
                    context.setAuthentication(authentication);
                    SecurityContextHolder.setContext(context);

                }
            } catch (Exception e) {
                logger.error("JWT token validation failed", e);
            }
        }

        // Continue with the request
        filterChain.doFilter(request, response);
    }
}

