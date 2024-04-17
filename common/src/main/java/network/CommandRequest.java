package network;

import common.commands.abstractions.Command;

public class CommandRequest extends Request {
    protected final Command command;

    public CommandRequest(Command c) {
        command = c;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public Object getContent() {
        return getCommand();
    }
}
