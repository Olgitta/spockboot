package com.olg.core.filters;

import com.olg.core.auth.dto.UserClaims;
import com.olg.core.utils.SecurityUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

//@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);

            String apiVersion = httpRequest.getHeader("API-Version");
            if (apiVersion != null) {
                MDC.put("apiVersion", apiVersion);
            }

            Authentication auth = SecurityUtils.getAuthentication();

            if (auth != null && auth.isAuthenticated()) {
                UserClaims userClaims = (UserClaims) auth.getPrincipal();
                MDC.put("uid", userClaims.guid());
                MDC.put("username", userClaims.username());
                MDC.put("email", userClaims.email());
            }

            chain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}

