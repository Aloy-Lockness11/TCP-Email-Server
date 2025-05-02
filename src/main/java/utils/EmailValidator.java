package utils;
import exception.InvalidEmailDetailsException;
import jakarta.validation.*;
import model.Email;

import java.util.Set;
public class EmailValidator {
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    /**
     * Validates the email object using Jakarta Bean Validation
     * This method checks the pattern constraints i.e regex defined in the Email class
     * to validate an email.
     *
     * @param email Email object to validate
     * @throws ConstraintViolationException if validation fails
     */
    public static void validate(Email email) throws ConstraintViolationException {
        Set<ConstraintViolation<Email>> violations = validator.validate(email);

        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (ConstraintViolation<?> violation : violations) {
                message.append(violation.getMessage()).append("; ");
            }
            throw new InvalidEmailDetailsException(message.toString().trim());
        }
    }
}
