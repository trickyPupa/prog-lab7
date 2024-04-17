package common.exceptions;

public class InterruptException extends RuntimeException {
    public InterruptException() {
    }
    public InterruptException(String message) {
        super(message);
    }
}
