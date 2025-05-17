package com.olg.qweb.api.users;

import com.olg.qweb.api.ApiResponse;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserServiceFactory userServiceFactory;

    public UserController(UserServiceFactory userServiceFactory) {
        this.userServiceFactory = userServiceFactory;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IUserResponse>>> getUsers(@RequestHeader(name = "API-Version", required = false) String version) {

        try {
            IUserService service = userServiceFactory.getService(version);
            List<IUserResponse> response = service.getUsers();

            return ResponseEntity.ok(new ApiResponse<>(MDC.get("requestId"), response));
        } catch (Exception e) {
            log.error("Something went wrong", e);
            return ResponseEntity.badRequest().body(null);
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IUserResponse>> getUserById(@PathVariable String id,
                                                @RequestHeader(name = "API-Version", required = false) String version) {

        try {
            IUserService service = userServiceFactory.getService(version);
            IUserResponse response = service.getUserById(id);

            return ResponseEntity.ok(new ApiResponse<>(MDC.get("requestId"), response));
        } catch (Exception e) {
            log.error("Something went wrong", e);
            return ResponseEntity.badRequest().body(null);
        }

    }
}

