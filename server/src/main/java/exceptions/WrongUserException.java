package exceptions;

public class WrongUserException extends Exception {
    public WrongUserException() {
    }

    public WrongUserException(String message) {
        super(message);
    }

    public WrongUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongUserException(Throwable cause) {
        super(cause);
    }
}
