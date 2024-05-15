package common.commands.implementations;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class SaveCommand extends AbstractCommand {
    public SaveCommand(Object[] args){
        super("save", "Команда для сохранения текущей версии коллекции.", "no", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        rec.save(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        rec.save(getArgs());
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return SaveCommand::new;
    }
}
