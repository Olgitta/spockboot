package com.olg.core.auth.dto;

public record UserClaims(String username, String uid, String email) {

    @Override
    public String toString() {
        return "UserClaims{" +
                "username='" + username + '\'' +
                ", uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
