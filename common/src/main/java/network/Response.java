package network;

import java.io.Serializable;

public class Response implements Serializable {
    protected String message = null;

    public Response(String msg) {
        message = msg;
    }

    public String getMessage(){
        return message;
    }
}
