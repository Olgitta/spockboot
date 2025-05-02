package com.olg.qweb.api.users.v2;

import com.olg.qweb.api.users.IUserDto;

import java.util.Objects;

public class UserDto implements IUserDto {
    String uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserDto that = (UserDto) o;
        return Objects.equals(getUid(), that.getUid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUid());
    }

    @Override
    public String toString() {
        return "UserDataDto{" +
                "uid='" + uid + '\'' +
                '}';
    }
}
