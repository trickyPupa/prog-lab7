package common.commands.implementations;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class ShowCommand extends AbstractCommand {
    public ShowCommand(Object[] args) {
        super("show", "Команда для просмотра коллекции.", "no", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        rec.show(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        rec.show(getArgs());
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return ShowCommand::new;
    }
}
