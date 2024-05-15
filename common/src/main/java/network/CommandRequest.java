package network;

import common.commands.abstractions.Command;

import java.util.Collection;

public class CommandRequest extends Request {
    protected final Command command;
    protected final Collection<Command> history;

    public CommandRequest(Command c, Collection<Command> h) {
        command = c;
        history = h;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public Object getContent() {
        return getCommand();
    }

    @Override
    public String toString() {
        return "CommandRequest{" +
                "command=" + command +
                '}';
    }

    public Collection<Command> getHistory() {
        return history;
    }
}
