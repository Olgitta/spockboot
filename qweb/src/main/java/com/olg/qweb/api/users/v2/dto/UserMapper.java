package com.olg.qweb.api.users.v2.dto;

import com.olg.core.dbsql.users.User;
import com.olg.qweb.api.users.IUserResponse;

import java.util.List;

//@ApiVersion("1")
//@Component("UserDtoMapperV1")
public class UserMapper {

    public static IUserResponse map(User user) {
        UserResponse response = new UserResponse();
        response.setUserDetails(user.getGuid().toString(), user.getName(), user.getEmail());
        return response;
    }

    public static List<IUserResponse> map(List<User> users) {
        List<IUserResponse> response = users.stream()
                .map(UserMapper::map)
                .toList();

        return response;
    }
}
