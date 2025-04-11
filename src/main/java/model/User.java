package model;


import jakarta.validation.constraints.*;
import lombok.*;
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

    @NotBlank(message = "Password must not be blank")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long, include one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String password;

    private List<Email> emailsSent;
    private List<Email> emailsReceived;

    // Additional fields and methods can be added here as needed
}
