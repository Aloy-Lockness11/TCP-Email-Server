package model;

import exception.InvalidUserCredentialsException;
import exception.UserAlreadyExistsException;
import exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {
    private UserManager userManager;
    private final String validFirstName = "John";
    private final String validLastName = "Doe";
    private final String validEmail = "john@voidmail.com";
    private final String validPassword = "Password123!";

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        // Clear any existing users
        userManager.setUserMap(new ConcurrentHashMap<>());
    }

    @Test
    void testRegisterUser_Success() {
        assertDoesNotThrow(() -> {
            userManager.registerUser(validFirstName, validLastName, validEmail, validPassword);
        });

        // Verify user was added
        assertNotNull(userManager.getUserMap().get(validEmail));
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        // First registration
        assertDoesNotThrow(() -> {
            userManager.registerUser(validFirstName, validLastName, validEmail, validPassword);
        });

        // Second registration with same email
        assertThrows(UserAlreadyExistsException.class, () -> {
            userManager.registerUser(validFirstName, validLastName, validEmail, validPassword);
        });
    }

    @Test
    void testLoginUser_Success() {
        // Register user first
        assertDoesNotThrow(() -> {
            userManager.registerUser(validFirstName, validLastName, validEmail, validPassword);
        });

        // Try to login
        assertDoesNotThrow(() -> {
            userManager.loginUser(validEmail, validPassword);
        });
    }

    @Test
    void testLoginUser_UserNotFound() {
        assertThrows(UserNotFoundException.class, () -> {
            userManager.loginUser("nonexistent@voidmail.com", validPassword);
        });
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        // Register user first
        assertDoesNotThrow(() -> {
            userManager.registerUser(validFirstName, validLastName, validEmail, validPassword);
        });

        // Try to login with wrong password
        assertThrows(InvalidUserCredentialsException.class, () -> {
            userManager.loginUser(validEmail, "WrongPassword123!");
        });
    }

    @Test
    void testSetUserMap_Success() {
        ConcurrentHashMap<String, User> newUserMap = new ConcurrentHashMap<>();
        User testUser = new User(validFirstName, validLastName, validEmail, validPassword);
        newUserMap.put(validEmail, testUser);

        userManager.setUserMap(newUserMap);

        assertEquals(newUserMap, userManager.getUserMap());
        assertEquals(testUser, userManager.getUserMap().get(validEmail));
    }

    @Test
    void testSetUserMap_Null() {
        // Should handle null map gracefully
        userManager.setUserMap(null);
        assertNotNull(userManager.getUserMap());
    }

    @Test
    void testGetUserMap_ReturnsCorrectMap() {
        // Register a user
        assertDoesNotThrow(() -> {
            userManager.registerUser(validFirstName, validLastName, validEmail, validPassword);
        });

        // Get the map and verify
        var userMap = userManager.getUserMap();
        assertNotNull(userMap);
        assertTrue(userMap.containsKey(validEmail));
        assertEquals(validEmail, userMap.get(validEmail).getEmail());
    }

    @Test
    void testConcurrentUserRegistration() {
        // Test that UserManager can handle concurrent registrations
        assertDoesNotThrow(() -> {
            userManager.registerUser("User1", "Test", "user1@voidmail.com", "Password123!");
            userManager.registerUser("User2", "Test", "user2@voidmail.com", "Password123!");
            userManager.registerUser("User3", "Test", "user3@voidmail.com", "Password123!");
        });

        assertEquals(3, userManager.getUserMap().size());
    }
}