package exceptions;

public class ConnectionsFallsExcetion extends RuntimeException{
    public ConnectionsFallsExcetion() {
    }

    public ConnectionsFallsExcetion(String message) {
        super(message);
    }
}
