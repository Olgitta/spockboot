package com.olg.qweb.api.users.v2.dto;

import com.olg.mysql.users.User;

//@ApiVersion("1")
//@Component("UserDtoMapperV1")
public class UserMapper {

    public static UserResponse map(User user) {
        UserResponse response = new UserResponse();
        response.setUserDetails(user.getGuid().toString(), user.getName(), user.getEmail());
        return response;
    }
}
