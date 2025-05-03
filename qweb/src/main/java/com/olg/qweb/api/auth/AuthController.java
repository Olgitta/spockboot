package com.olg.qweb.api.auth;

import com.olg.qweb.api.auth.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

        try {
            AuthResponse rs = authService.login(request.email(), request.password());
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", rs.refreshToken())
                    .httpOnly(true) // protects from JS access
                    .secure(true) // ensures it’s only sent over HTTPS
                    .path("/api/auth/refresh") // only send this cookie on refresh calls
                    .maxAge(Duration.ofDays(7))
                    .sameSite("Strict") // or Lax/None, depending on your frontend deployment
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(rs);
        } catch (DataAccessResourceFailureException dataAccessResourceFailureException){
            log.error("dataAccessResourceFailureException", dataAccessResourceFailureException);
            return ResponseEntity.internalServerError().body(null);
        } catch (Exception e) {
            log.error("Exception", e);
            return ResponseEntity.internalServerError().body(null);
        }

    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue("refreshToken") String refreshToken) {

        try {
            return ResponseEntity.ok(authService.refresh(refreshToken));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }

    }

}
