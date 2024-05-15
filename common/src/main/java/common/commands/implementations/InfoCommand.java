package common.commands.implementations;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class InfoCommand extends AbstractCommand {
    public InfoCommand(Object[] args){
        super("info", "Команда для вывода информации о коллекции.", "no", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        rec.info(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        rec.info(getArgs());
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return InfoCommand::new;
    }
}
