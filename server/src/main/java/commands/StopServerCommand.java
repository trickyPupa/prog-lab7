package commands;

import common.commands.abstractions.Command;
import managers.ServerControlReceiver;

import java.util.function.Function;

public class StopServerCommand extends AbstractServerCommand{
    public StopServerCommand(Object[] args) {
        super("stop", "Завершает работу сервера.", "no", args);
    }

    @Override
    public int execute(ServerControlReceiver rec) {
        return rec.stop();
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return StopServerCommand::new;
    }
}
