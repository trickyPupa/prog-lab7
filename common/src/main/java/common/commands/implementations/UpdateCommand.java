package common.commands.implementations;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class UpdateCommand extends AbstractCommand {
    public UpdateCommand(Object[] args) {
        super("update", "Команда для обновления значений элемента коллекции с заданным id. " +
                        "Если элемент создан не вами, удаления не произойдет.",
                "id {element}", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        rec.update(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        rec.update(getArgs());
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return UpdateCommand::new;
    }
}
