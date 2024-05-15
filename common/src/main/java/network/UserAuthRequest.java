package network;

import common.commands.abstractions.Command;

import java.util.Collection;

public class UserAuthRequest extends CommandRequest {
    public UserAuthRequest(Command c) {
        super(c, null);
    }
}
