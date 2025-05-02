package exception;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String emailId) {
        super("Email not found with ID: " + emailId);
    }
}