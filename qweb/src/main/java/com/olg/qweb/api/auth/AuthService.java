package com.olg.qweb.api.auth;

import com.olg.core.auth.JwtService;
import com.olg.core.utils.PasswordEncoder;
import com.olg.core.dbsql.users.User;
import com.olg.core.dbsql.users.UserRepository;
import com.olg.qweb.api.auth.dto.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtService jwtService,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(String email, String password) throws Exception {

        log.info("User login {}", email);

        // TODO: validate input
        User user = userRepository.findByEmail(email).orElseThrow();

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            // todo: create custom exception
            throw new Exception("User not found");
        }
        //todo: validate
        JwtService.TokenPair tokens = jwtService.generateAccessAndRefreshToken(
                user.getName(),
                user.getEmail(),
                user.getGuid().toString(),
                user.getId());
        return new AuthResponse(tokens.accessToken(), tokens.refreshToken());
    }

    public AuthResponse refresh(String refreshToken) throws Exception {
        String token = jwtService.generateAccessToken(refreshToken);
        if (token == null) {
            //todo: create custom exception
            throw new Exception("Login required");
        }
        return new AuthResponse(token, refreshToken);
    }
}

