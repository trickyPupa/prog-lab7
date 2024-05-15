package common.commands.implementations;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class HistoryCommand extends AbstractCommand {
    public HistoryCommand(Object[] args) {
        super("history","Команда для вывода последних 5 выполненных команд (без аргументов).",
                "no", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        rec.history(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        rec.history(getArgs());
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return HistoryCommand::new;
    }
}
