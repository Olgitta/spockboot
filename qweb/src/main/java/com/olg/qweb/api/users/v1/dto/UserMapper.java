package com.olg.qweb.api.users.v1.dto;

import com.olg.mysql.users.User;

public class UserMapper {

    public static UserResponse map(User user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getName());
        response.setEmail(user.getEmail());
        response.setUid(user.getGuid().toString());
        return response;
    }
}
