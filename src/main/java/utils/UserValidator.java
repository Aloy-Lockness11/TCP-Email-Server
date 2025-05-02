package utils;
import exception.InvalidUserDetailsException;
import jakarta.validation.*;
import model.User;

import java.util.Set;
public class UserValidator {
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    /**
     * Validates the user object using Jakarta Bean Validation
     * This method checks the patern constraints i.e regex defind in the User class
     * to validate a user.
     *
     * @param user User object to validate
     * @throws ConstraintViolationException if validation fails
     */
    public static void validate(User user) throws ConstraintViolationException {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            for (ConstraintViolation<?> violation : violations) {
                msg.append(violation.getMessage()).append("; ");
            }
            throw new InvalidUserDetailsException(msg.toString().trim());
        }
    }
}
