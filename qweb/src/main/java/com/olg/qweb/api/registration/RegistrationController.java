package com.olg.qweb.api.registration;

import com.olg.qweb.api.registration.dto.RegistrationRequest;
import com.olg.qweb.api.registration.dto.RegistrationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest request) {

        try {
            RegistrationService.RegistrationResult result = registrationService.register(request.username(), request.email(), request.password());
            return ResponseEntity.ok(
                    new RegistrationResponse(
                            result.email(),
                            result.username(),
                            result.uid()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }

    }

}
