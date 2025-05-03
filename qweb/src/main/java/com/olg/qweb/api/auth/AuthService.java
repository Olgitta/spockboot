package com.olg.qweb.api.auth;

import com.olg.core.auth.JwtService;
import com.olg.core.utils.PasswordEncoder;
import com.olg.mysql.users.User;
import com.olg.mysql.users.UserRepository;
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

    public record AuthTokens (String accessToken, String refereshToken) {}

    public AuthTokens login(String email, String password) throws Exception {

        log.info("User login {}", email);
        // TODO: validate input

        User found = userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .orElse(null);

        if(found == null){
            // todo: create custom exception
            throw new Exception("User not found");
        }
        String token = jwtService.generateAccessToken(found.getName(), found.getEmail(), found.getGuid().toString());
        String refreshToken = jwtService.generateRefreshToken(found.getName(), found.getEmail(), found.getGuid().toString());
        return new AuthTokens(token, refreshToken);

    }

    public AuthTokens refresh(String refreshToken) throws Exception {
        String token = jwtService.generateAccessToken(refreshToken);
        if(token==null) {
            //todo: create custom exception
            throw new Exception("Login required");
        }
        return new AuthTokens(token, null);
    }
}

