package com.olg.qweb.api.users.v2;

import com.olg.core.annotations.ApiVersion;
import com.olg.mysql.users.User;
import com.olg.mysql.users.UserRepository;
import com.olg.qweb.api.users.IUserService;
import com.olg.qweb.api.users.v2.dto.UserMapper;
import com.olg.qweb.api.users.v2.dto.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@ApiVersion("2")
@Service("UserServiceV2")
public class UserService implements IUserService {

    private final String version = this.getClass().getAnnotation(ApiVersion.class).value();
    private static final Logger log = LoggerFactory.getLogger(com.olg.qweb.api.users.v1.UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse getUserById(String id) {
        log.debug("getUserById - {} - ApiVersion - {}", id, version);

        User user = userRepository.findByGuid(UUID.fromString(id)).orElseThrow();

        return UserMapper.map(user);
    }

    @Override
    public String getVersion() {
        return this.version;
    }
}
