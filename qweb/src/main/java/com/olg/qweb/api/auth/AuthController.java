package com.olg.qweb.api.auth;

import com.olg.qweb.api.auth.dto.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthService.AuthTokens tokens = authService.login(request.email(), request.password());
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.refereshToken())
                .httpOnly(true) // protects from JS access
                .secure(true) // ensures it’s only sent over HTTPS
                .path("/api/auth/refresh") // only send this cookie on refresh calls
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict") // or Lax/None, depending on your frontend deployment
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue("refreshToken") String refreshToken) {
        AuthService.AuthTokens tokens = authService.refresh(refreshToken);
        return ResponseEntity.ok(new AuthResponse(tokens.accessToken()));
    }

}
