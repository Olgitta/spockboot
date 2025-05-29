package com.olg.qweb.api.users;

import com.olg.core.annotations.ApiVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

public class UserServiceFactoryTest {

    @ApiVersion("v1")
    static class UserServiceV1 implements IUserService {
        /**
         * @param id
         * @return
         */
        @Override
        public IUserResponse getUserById(String id) {
            return null;
        }

        /**
         * @return
         */
        @Override
        public String getVersion() {
            return "";
        }

        /**
         * @return
         */
        @Override
        public List<IUserResponse> getUsers() {
            return List.of();
        }
    }

    @ApiVersion("v2")
    static class UserServiceV2 implements IUserService {
        /**
         * @param id
         * @return
         */
        @Override
        public IUserResponse getUserById(String id) {
            return null;
        }

        /**
         * @return
         */
        @Override
        public String getVersion() {
            return "";
        }

        /**
         * @return
         */
        @Override
        public List<IUserResponse> getUsers() {
            return List.of();
        }
    }

    static class NoVersionService implements IUserService {
        /**
         * @param id
         * @return
         */
        @Override
        public IUserResponse getUserById(String id) {
            return null;
        }

        /**
         * @return
         */
        @Override
        public String getVersion() {
            return "";
        }

        /**
         * @return
         */
        @Override
        public List<IUserResponse> getUsers() {
            return List.of();
        }
    }

    @Test
    @DisplayName("Should return correct service based on version")
    void getService_shouldReturnCorrectVersion() throws Exception {
        // Arrange
        IUserService v1 = new UserServiceV1();
        IUserService v2 = new UserServiceV2();
        List<IUserService> services = Arrays.asList(v1, v2);

        UserServiceFactory factory = new UserServiceFactory(services);

        // Act
        IUserService resultV1 = factory.getService("v1");
        IUserService resultV2 = factory.getService("v2");

        // Assert
        assertSame(v1, resultV1);
        assertSame(v2, resultV2);
    }

    @Test
    @DisplayName("Should throw exception for unknown version")
    void getService_shouldThrowExceptionForUnknownVersion() {
        // Arrange
        IUserService v1 = new UserServiceV1();
        List<IUserService> services = List.of(v1);

        UserServiceFactory factory = new UserServiceFactory(services);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> factory.getService("v999"));
        assertEquals("Not supported service version", exception.getMessage());
    }

    @Test
    @DisplayName("Should ignore services without ApiVersion annotation")
    void getService_shouldIgnoreServiceWithoutAnnotation() {
        // Arrange
        IUserService v1 = new UserServiceV1();
        IUserService noVersion = new NoVersionService();
        List<IUserService> services = List.of(v1, noVersion);

        UserServiceFactory factory = new UserServiceFactory(services);

        // Only v1 is mapped
        assertDoesNotThrow(() -> {
            assertSame(v1, factory.getService("v1"));
        });

        Exception exception = assertThrows(Exception.class, () -> factory.getService("v0"));
        assertEquals("Not supported service version", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception for null version")
    void getService_shouldThrowForNullVersion() {
        // Arrange
        IUserService v1 = new UserServiceV1();
        List<IUserService> services = List.of(v1);

        UserServiceFactory factory = new UserServiceFactory(services);

        Exception exception = assertThrows(Exception.class, () -> factory.getService(null));
        assertEquals("Not supported service version", exception.getMessage());
    }
}
