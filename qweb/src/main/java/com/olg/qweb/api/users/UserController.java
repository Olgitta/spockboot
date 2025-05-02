package com.olg.qweb.api.users;

import com.olg.core.utils.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
public class UserController<T> {

    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserServiceFactory userServiceFactory;
    private final UserDtoMapperFactory userDtoMapperFactory;

    public UserController(UserServiceFactory userServiceFactory,
                          UserDtoMapperFactory userDtoMapperFactory) {
        this.userServiceFactory = userServiceFactory;
        this.userDtoMapperFactory = userDtoMapperFactory;
    }

    @GetMapping("/test")
    public String test(@RequestHeader(name = "API-Version", required = false) String version) {
        return "Hello, " + SecurityUtils.getUsername();
    }

    @GetMapping("/{id}")
    public ResponseEntity<IUserDto> getUserById(@PathVariable String id,
                                                @RequestHeader(name = "API-Version", required = false) String version) {

        try {
            IUserService service = userServiceFactory.getService(version);
            IUserDtoMapper mapper = userDtoMapperFactory.getMapper(version);
            Object data = service.getUserById(id);
            return ResponseEntity.ok(mapper.map(data));
        } catch (Exception e) {
            log.error("Something went wrong", e);
            return ResponseEntity.badRequest().body(null);
        }

    }
}

