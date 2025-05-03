package com.olg.qweb.api.users;

public interface IUserService {
    IUserResponse getUserById(String id);
    String getVersion();
}
