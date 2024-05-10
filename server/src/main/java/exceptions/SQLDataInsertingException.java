package exceptions;

import java.sql.SQLException;

public class SQLDataInsertingException extends SQLException {
    public SQLDataInsertingException() {
        super();
    }

    public SQLDataInsertingException(Throwable cause) {
        super(cause);
    }

    public SQLDataInsertingException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
