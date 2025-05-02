package utils;
import jakarta.validation.*;
import model.User;
import java.util.Set;

public class UserValidator {
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    public static void validate(User user) throws ConstraintViolationException {
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("User validation failed", violations);
        }
    }
}
