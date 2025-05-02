package model;

import exception.InvalidUserCredentialsException;
import exception.*;

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

    void loginUser(String email, String password)
            throws InvalidUserCredentialsException, UserNotFoundException;

}
