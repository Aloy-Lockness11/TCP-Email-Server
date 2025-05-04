package exception;

public class FailedToSaveException extends Exception {
    public FailedToSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
