package common.commands.implementations;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class ClearCommand extends AbstractCommand {
    public ClearCommand(Object[] args){
        super("clear", "Команда для удаления всех элементов коллекции, созданных вами.", "no", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        rec.clear(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        rec.clear(getArgs());
    }
    @Override
    public Function<Object[], Command> getConstructor() {
        return ClearCommand::new;
    }
}
