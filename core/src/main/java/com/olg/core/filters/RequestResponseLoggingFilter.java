package com.olg.core.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

//@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Skip logging for /h2-console and its subpaths
        if (uri.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
//        String requestId = MDC.get("requestId");

        log.info("Request {} {}", method, uri);

        // Wrap the response to capture status after filter chain
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(request, wrappedResponse);
        } finally {
            int status = wrappedResponse.getStatus();
            long duration = System.currentTimeMillis() - startTime;

            log.info("Response {} {} {} {}ms",
                    method,
                    uri,
                    status,
                    duration);

            wrappedResponse.copyBodyToResponse(); // copy content back to real response
        }
    }
}
