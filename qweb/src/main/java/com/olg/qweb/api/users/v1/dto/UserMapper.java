package com.olg.qweb.api.users.v1.dto;

import com.olg.mysql.users.User;
import com.olg.qweb.api.users.IUserResponse;

import java.util.List;

public class UserMapper {

    public static IUserResponse map(User user) {
        UserResponse response = new UserResponse();
        response.setUsername(user.getName());
        response.setEmail(user.getEmail());
        response.setUid(user.getGuid().toString());
        return response;
    }

    public static List<IUserResponse> map(List<User> users) {
        List<IUserResponse> response = users.stream()
                .map(UserMapper::map)
                .toList();

        return response;
    }
}
