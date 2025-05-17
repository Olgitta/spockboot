package com.olg.qweb.api.users;

import java.util.List;

public interface IUserService {
    IUserResponse getUserById(String id);
    String getVersion();

    List<IUserResponse> getUsers();
}
