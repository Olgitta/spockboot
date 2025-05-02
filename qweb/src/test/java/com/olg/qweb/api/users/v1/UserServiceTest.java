package com.olg.qweb.api.users.v1;

import com.olg.qweb.api.users.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Test
    void getUserById() {
        IUserService svc = new UserService();
        UserService.UserData user = (UserService.UserData) svc.getUserById("7777");
        System.out.println(user);
        assertEquals("7777", user.uid());
    }
}