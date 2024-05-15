package common.commands.implementations;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class MinByCoordinatesCommand extends AbstractCommand {
    public MinByCoordinatesCommand(Object[] args) {
        super("min_by_coordinates", "Команда для вывода элемента коллекции с минимальными координатами.",
                "no", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        rec.minByCoordinates(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        rec.minByCoordinates(getArgs());
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return MinByCoordinatesCommand::new;
    }
}
