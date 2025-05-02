package com.olg.qweb.api.users;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceFactoryTest {

    @Autowired
    UserServiceFactory userServiceFactory;

    @Test
    void getService() throws Exception {
        IUserService svc_v1 = userServiceFactory.getService("1");
        assertEquals("1", svc_v1.getVersion());

        assertThrows(Exception.class, () -> userServiceFactory.getService("10000"));
    }
}