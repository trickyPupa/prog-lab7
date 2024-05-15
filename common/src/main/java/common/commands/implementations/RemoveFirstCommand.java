package common.commands.implementations;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class RemoveFirstCommand extends AbstractCommand {
    public RemoveFirstCommand(Object[] arg) {
        super("remove_first", "Команда для удаления первого найденного элемента коллекции, созданного вами.",
                "no", arg);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        rec.removeFirst(s);
    }

    @Override
    public void execute(AbstractReceiver rec) {
        rec.removeFirst(getArgs());
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return RemoveFirstCommand::new;
    }
}
