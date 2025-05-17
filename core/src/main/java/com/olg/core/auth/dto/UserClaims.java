package com.olg.core.auth.dto;

public record UserClaims(String username, String guid, String email, Long id) {

    @Override
    public String toString() {
        return "UserClaims{" +
                "username='" + username + '\'' +
                ", guid='" + guid + '\'' +
                ", email='" + email + '\'' +
                ", id=" + id +
                '}';
    }
}
