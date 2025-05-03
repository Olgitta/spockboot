package com.olg.qweb.api.users.v2.dto;

import com.olg.qweb.api.ApiResponse;
import com.olg.qweb.api.users.IUserResponse;

public class UserResponse implements IUserResponse {

    public record UserDetails(String uid, String username, String email){};

    private UserDetails userDetails;

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(String uid, String username, String email) {
        this.userDetails = new UserDetails(uid, username, email);
    }
}
