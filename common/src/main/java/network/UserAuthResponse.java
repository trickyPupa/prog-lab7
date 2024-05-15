package network;

import common.commands.abstractions.Command;

import java.util.Collection;

public class UserAuthResponse extends Response {
    private boolean status;

    public UserAuthResponse(String msg, boolean status, Collection<Command> h) {
        super(msg, h);
        this.status = status;
    }
    public boolean getStatus() {
        return status;
    }
}
