package model;

import exception.InvalidUserDetailsException;
import exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        userManager.setUserMap(new ConcurrentHashMap<>()); // clear existing state
    }

    @Test
    void testRegisterUser_validUser_shouldSucceed() {
        assertDoesNotThrow(() -> {
            userManager.registerUser("Alice", "Smith", "alice@voidmail.com", "StrongPass1!");
        });
    }


    @Test
    void testRegisterUser_duplicateEmail_shouldThrow() {
        assertDoesNotThrow(() -> {
            userManager.registerUser("Bob", "Smith", "bob@voidmail.com", "Password123!");
        });

        assertThrows(UserAlreadyExistsException.class, () -> {
            userManager.registerUser("Bobby", "Smith", "bob@voidmail.com", "Another1!");
        });
    }

    @Test
    void testRegisterUser_invalidEmail_shouldThrow() {
        assertThrows(InvalidUserDetailsException.class, () -> {
            userManager.registerUser("Tom", "Brown", "invalid-email", "123");
        });
    }
}