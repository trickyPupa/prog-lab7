package common.exceptions;

public class WrongArgumentException extends RuntimeException {
    public WrongArgumentException(String message) {
        super(message);
    }
    public WrongArgumentException() {
    }

    @Override
    public String toString() {
        return "Некорректные аргументы : " + getMessage() + ".";
    }
}
