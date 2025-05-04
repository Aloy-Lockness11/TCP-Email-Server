package model;

import exception.InvalidUserCredentialsException;
import exception.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface UserManagerInterface {

    /**
     * Registers a new user with the provided details.
     *
     * @param firstName the first name of the user
     * @param lastName the last name of the user
     * @param email the email of the user
     * @param password the password of the user
     * @throws UserAlreadyExistsException if a user with the same email already exists
     * @throws InvalidUserDetailsException if any of the user details are invalid
     */
    void registerUser(String firstName, String lastName, String email, String password)
            throws UserAlreadyExistsException, InvalidUserDetailsException;

    /**
     * Logs in a user with the provided email and password.
     *
     * @param email the email of the user
     * @param password the password of the user
     * @throws InvalidUserCredentialsException if the credentials are invalid
     * @throws UserNotFoundException if the user is not found
     */
    void loginUser(String email, String password)
            throws InvalidUserCredentialsException, UserNotFoundException;

    /**
     * Sets the user map.
     *
     * @param userMap the user map to set
     */
    void setUserMap(ConcurrentHashMap<String, User> userMap);

    /**
     * Gets the user map.
     *
     * @return the user map
     */
    Map<String, User> getUserMap();
}
