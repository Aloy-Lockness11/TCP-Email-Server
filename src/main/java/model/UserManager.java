package model;

import exception.*;
import lombok.extern.slf4j.Slf4j;
import utils.SecurityUtils;
import utils.validators.UserValidator;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    public void registerUser(String firstName, String lastName, String email, String password)
            throws UserAlreadyExistsException, InvalidUserDetailsException, PasswordEncryptionException {

        if (users.containsKey(email)) {
            throw new UserAlreadyExistsException(email);
        }

        try {
            // Create user with raw password for validation
            User user = new User(firstName, lastName, email, password);

            // Validate while password is still there
            UserValidator.validate(user);

            // Hash the password after validation
            String salt = SecurityUtils.generateSalt();
            String hashedPassword = SecurityUtils.hashPassword(password, salt);

            // Store secure credentials
            user.setSalt(salt);
            user.setHashedPassword(hashedPassword);

            // Clear the raw password
            user.setPassword(null);

            users.put(email, user);
            log.info("User registered: {}", email);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error hashing password for {}: {}", email, e.getMessage());
            throw new PasswordEncryptionException("Error while hashing password during registration", e);
        }
    }


    /**
     * Logs in a user.
     *
     * @param email    Email of the user
     * @param password Password of the user
     * @throws InvalidUserCredentialsException if the password is invalid
     * @throws UserNotFoundException if the user is not found
     */
    public void loginUser(String email, String password) throws InvalidUserCredentialsException, UserNotFoundException, PasswordEncryptionException {
        User user = users.get(email);
        if (user == null) {
            log.warn("User not found: {}", email);
            throw new UserNotFoundException(email);
        }

        try {
            boolean isMatch = SecurityUtils.verifyPassword(password, user.getSalt(), user.getHashedPassword());
            if (!isMatch) {
                log.warn("Invalid password for user: {}", email);
                throw new InvalidUserCredentialsException(email);
            }
            log.info("User logged in successfully: {}", email);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error verifying password for {}: {}", email, e.getMessage());
            throw new PasswordEncryptionException("Error while authenticating using hash exception XP: ", e);
        }
    }

    /**
     * Gets all logged-in users from concurrent hash map.
     */
    @Override
    public List<User> getLoggedInUsers() {
        return users.values().stream()
                .filter(User::isLoggedIn)
                .collect(Collectors.toList());
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

    /**
     * Sets the logged-in status of a user.
     * in the concurrent hash map. if the user is not found, it does nothing.
     *
     * @param email  the email of the user
     * @param status the logged-in status to set
     */
    @Override
    public void setLoggedIn(String email, boolean status) {
        User user = users.get(email);
        if (user != null) {
            user.setLoggedIn(status);
        }else {
            log.warn("User not found for setting logged-in status: {}", email);
        }
    }
}
