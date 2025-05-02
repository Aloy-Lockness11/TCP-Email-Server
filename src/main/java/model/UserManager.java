package model;

import exception.InvalidUserCredentialsException;
import exception.InvalidUserDetailsException;
import exception.UserAlreadyExistsException;
import exception.UserNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import utils.UserValidator;

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

        // Validate user object
        try {
            UserValidator.validate(user);
        } catch (ConstraintViolationException e) {
            StringBuilder msg = new StringBuilder();
            for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
                msg.append(violation.getMessage()).append("; ");
                log.warn("Validation error for : {}", violation.getMessage());
            }
            throw new InvalidUserDetailsException(msg.toString());
        }

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
            log.warn("Invalid password for user: {}", email);
            throw new InvalidUserCredentialsException(email);
        }

        log.info("User logged in successfully: {}", email);
    }
}
