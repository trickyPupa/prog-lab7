package network;

import common.commands.abstractions.Command;

import java.io.Serializable;
import java.util.Collection;

public class Response implements Serializable {
    protected String message = null;
    protected Collection<Command> history;

    public Response(String msg, Collection<Command> history) {
        message = msg;
        this.history = history;
    }

    public String getMessage(){
        return message;
    }

    public Collection<Command> getHistory(){
        return history;
    }
}
