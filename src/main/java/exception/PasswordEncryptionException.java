package exception;

public class PasswordEncryptionException extends RuntimeException {
    public PasswordEncryptionException(String message) {
        super(message);
    }

    public PasswordEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
