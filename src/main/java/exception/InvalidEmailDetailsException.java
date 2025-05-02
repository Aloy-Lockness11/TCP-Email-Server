package exception;

public class InvalidEmailDetailsException extends RuntimeException {
    public InvalidEmailDetailsException(String message) {
        super(message);
    }
}