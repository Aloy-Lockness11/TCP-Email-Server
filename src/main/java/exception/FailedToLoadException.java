package exception;

public class FailedToLoadException extends Exception {
    public FailedToLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
