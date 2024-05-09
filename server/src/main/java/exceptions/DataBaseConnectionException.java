package exceptions;

public class DataBaseConnectionException extends Exception{
    public DataBaseConnectionException() {
    }

    public DataBaseConnectionException(String message) {
        super(message);
    }

    public DataBaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataBaseConnectionException(Throwable cause) {
        super(cause);
    }
}
