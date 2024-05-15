package network;

import common.commands.abstractions.Command;

import java.util.Collection;

public class UserRegisterRequest extends UserAuthRequest {
    String salt;

    public UserRegisterRequest(Command c, String salt) {
        super(c);
        this.salt = salt;
    }

    public String getSalt() {
        return salt;
    }
}
