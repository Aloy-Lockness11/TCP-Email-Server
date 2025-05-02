package model;

import exception.InvalidUserCredentialsException;
import exception.UserAlreadyExistsException;
import exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * UserManager class to manage user registration and login.
 * It uses a HashMap to store user data.
 */
@Slf4j
public class UserManager {
    private static final Map<String, User> users = new HashMap<>();

    public static void registerUser(String firstName, String lastName, String email, String password) throws UserAlreadyExistsException {
        if (users.containsKey(email)) {
            throw new UserNotFoundException(email);// User already exists
        }

        User user = new User(firstName, lastName, email, password);
        users.put(email, user);
        log.info("User registered successfully: {}", email);
    }

    public static void loginUser(String email, String password) throws InvalidUserCredentialsException, UserNotFoundException {

        User user = users.get(email);

        if(user == null) {
            log.error("User not found: {}", email);
            throw new UserNotFoundException(email);
        }

        if(!user.getPassword().equals(password)) {
            log.error("Invalid password for user: {}", email);
            throw new InvalidUserCredentialsException(email);
        }

        log.info("User logged in successfully: {}", email);
    }
}
