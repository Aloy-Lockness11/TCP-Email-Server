package model;

import exception.InvalidUserCredentialsException;
import exception.InvalidUserDetailsException;
import exception.UserAlreadyExistsException;
import exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import utils.validators.UserValidator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserManager class to manage user registration and login.
 * It uses a HashMap to store user data.
 */
@Slf4j
public class UserManager implements UserManagerInterface {

    // HashMap to store user data uses ConcurrentHashMap for thread safety
    private static final Map<String, User> users = new ConcurrentHashMap<>();;

    /**
     * Registers a new user.
     * if the user already exists, it throws UserAlreadyExistsException.
     * If the user details are invalid, it throws InvalidUserDetailsException.
     * The details being validated are validated using the UserValidator class.
     *
     * @param firstName First name of the user
     * @param lastName  Last name of the user
     * @param email     Email of the user
     * @param password  Password of the user
     * @throws UserAlreadyExistsException if the user already exists
     * @throws InvalidUserDetailsException if the user details are invalid
     */
    public void registerUser(String firstName, String lastName, String email, String password) throws UserAlreadyExistsException {
        if (users.containsKey(email)) {
            throw new UserAlreadyExistsException(email);
        }

        User user = new User(firstName, lastName, email, password);
        UserValidator.validate(user); // Now cleaner

        users.put(email, user);
        log.info("User registered: {}", email);
    }


    /**
     * Logs in a user.
     *
     * @param email    Email of the user
     * @param password Password of the user
     * @throws InvalidUserCredentialsException if the password is invalid
     * @throws UserNotFoundException if the user is not found
     */
    public void loginUser(String email, String password) throws InvalidUserCredentialsException, UserNotFoundException {

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

    /**
     * Sets the user map
     * This method is used to load user data into memory
     * It clears the existing user map and loads the new data
     *
     * @param userMap The user map to set
     */
    @Override
    public void setUserMap(ConcurrentHashMap<String, User> userMap) {
        if (userMap != null) {
            users.clear();
            users.putAll(userMap);
            log.info("User data loaded into memory. Total users: {}", users.size());
        } else {
            log.warn("Attempted to load null user data. Skipping.");
        }
    }

    /**
     * Gets the user map.  From the concurrent hash map.
     *
     * @return the user map
     */
    @Override
    public Map<String, User> getUserMap() {
        return users;
    }
}
