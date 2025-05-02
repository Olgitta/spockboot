package com.olg.qweb.api.users.v1;

import com.olg.core.annotations.ApiVersion;
import com.olg.qweb.api.users.IUserDto;
import com.olg.qweb.api.users.IUserDtoMapper;
import org.springframework.stereotype.Component;

@ApiVersion("1")
@Component("UserDtoMapperV1")
public class UserDtoMapper implements IUserDtoMapper {

    @Override
    public IUserDto map(Object o) {
        return null;
    }
}
