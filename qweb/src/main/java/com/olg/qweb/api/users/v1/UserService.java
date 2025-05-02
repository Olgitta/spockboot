package com.olg.qweb.api.users.v1;

import com.olg.core.annotations.ApiVersion;
import com.olg.qweb.api.users.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@ApiVersion("1")
@Service("UserServiceV1")
public class UserService implements IUserService {

    public record UserData (String uid){};
    private final String version = this.getClass().getAnnotation(ApiVersion.class).value();
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Override
    public UserData getUserById(String id) {
        log.debug("getUserById - {} - ApiVersion - {}", id, version);

        return new UserData(id);
    }

    @Override
    public String getVersion() {
        return this.version;
    }
}
