package common.commands.implementations;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class RemoveLowerCommand extends AbstractCommand {
    public RemoveLowerCommand(Object[] args) {
        super("remove_lower", "Команда для удаления всех элементов коллекции, меньших чем заданный и созданных вами.",
                "{element}", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        rec.removeLower(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        rec.removeLower(getArgs());
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return RemoveLowerCommand::new;
    }
}
