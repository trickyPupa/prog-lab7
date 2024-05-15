package common.commands.implementations;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class RemoveByIdCommand extends AbstractCommand {
    public RemoveByIdCommand(Object[] args) {
        super("remove_by_id", "Команда для удаления элемента коллекции с заданным id. " +
                        "Если элемент создан не вами, удаления не произойдет.",
                "id", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        rec.removeById(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        rec.removeById(getArgs());
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return RemoveByIdCommand::new;
    }
}
