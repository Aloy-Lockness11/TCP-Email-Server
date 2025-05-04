package model;


import jakarta.validation.constraints.*;
import lombok.*;

import java.beans.Transient;
import java.util.List;
import java.util.UUID;

/**
 * User class representing a user in the system.
 * This class is a placeholder and can be extended with additional fields and methods as needed.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {

    @NonNull
    private UUID id;

    @NotBlank(message = "First name must not be blank")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name must not be blank")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email must not be blank")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@voidmail\\.com$",
            message = "Email must be a valid voidmail.com address"
    )
    private String email;

    // Hashed password
    private String hashedPassword;

    // Salt used in password hashing
    private String salt;

    @NotBlank(message = "Password must not be blank")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long, include one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private transient String password;

    private boolean isLoggedIn;

    private List<Email> emailsSent;
    private List<Email> emailsReceived;

    /**
     * Constructor to create a new user with the provided details.
     *
     * @param firstName the first name of the user
     * @param lastName the last name of the user
     * @param email the email of the user
     * @param password the password of the user
     */
    // Constructor to store plain credentials
    public User(String firstName, String lastName, String email, String password) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    /**
     * Constructor to create a new user with hashed credentials.
     *
     * @param firstName the first name of the user
     * @param lastName the last name of the user
     * @param email the email of the user
     * @param hashedPassword the hashed password of the user
     * @param salt the salt used in password hashing
     */
    // Constructor to store hashed credentials
    public User(String firstName, String lastName, String email, String hashedPassword, String salt) {
        this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.salt = salt;
    }
}
