package commands;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;
import managers.ServerControlReceiver;

import java.util.function.Function;

public class DisconnectServerCommand extends AbstractServerCommand {
    public DisconnectServerCommand(Object[] args) {
        super("disconnect", "Завершает текущее соединение.", "no", args);
    }

    @Override
    public int execute(ServerControlReceiver rec) {
        return rec.disconnect();
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return DisconnectServerCommand::new;
    }
}
