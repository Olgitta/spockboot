package com.olg.qweb.api.users.v1;

import com.olg.core.utils.PasswordEncoder;
import com.olg.mysql.users.User;
import com.olg.mysql.users.UserRepository;
import com.olg.qweb.api.users.IUserService;
import com.olg.qweb.api.users.v1.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void getUserById() {

        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = new PasswordEncoder();

        String password = "1234567";
        UUID guid = UUID.randomUUID();

        User user = new User();
        user.setName("alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setGuid(guid);

        when(userRepository.findByGuid(guid)).thenReturn(Optional.of(user));

        IUserService svc = new UserService(userRepository);
        UserResponse rs = (UserResponse) svc.getUserById(guid.toString());
        System.out.println(rs);
        assertEquals(guid.toString(), rs.getUid());
    }
}