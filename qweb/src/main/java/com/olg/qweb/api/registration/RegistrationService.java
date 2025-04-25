package com.olg.qweb.api.registration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    public record RegistrationResult (String username, String email, String uid) {}

    public RegistrationResult register(String username, String email, String password) {
        // TODO: Register user and redirect to login
        log.info("User registration {}-{}", username, email);
        return new RegistrationResult(null, null, null);
    }
}

