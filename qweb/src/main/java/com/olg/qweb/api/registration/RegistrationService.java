package com.olg.qweb.api.registration;

import com.olg.core.utils.PasswordEncoder;
import com.olg.mysql.users.User;
import com.olg.mysql.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    public record RegistrationResult (String username, String email, String uid) {}

    public RegistrationService (PasswordEncoder passwordEncoder,
                                UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public RegistrationResult register(String username, String email, String password) throws Exception {
        // TODO: validate username, email, password
        if (userRepository.existsByEmail(email)) {
            log.info("User registration {}-{}, user exists", username, email);
            // TODO: create custom exception
            throw new Exception("User exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setName(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        User created = userRepository.save(user);

        log.info("User registration - {}", created);
        return new RegistrationResult(created.getName(), created.getEmail(), created.getGuid().toString());
    }
}

