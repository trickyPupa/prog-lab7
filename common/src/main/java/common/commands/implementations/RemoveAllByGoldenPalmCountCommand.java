package common.commands.implementations;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class RemoveAllByGoldenPalmCountCommand extends AbstractCommand {
    public RemoveAllByGoldenPalmCountCommand(Object[] args) {
        super("remove_all_by_golden_palm_count", "Команда для удаления всех элементов коллекции, созданных вами, " +
                        "с заданным количеством золотых пальмовых ветвей.",
                "goldenPalmCount", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        rec.removeAllByGoldenPalmCount(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        rec.removeAllByGoldenPalmCount(getArgs());
    }

    @Override
    public Function<Object[], Command> getConstructor() {
        return RemoveAllByGoldenPalmCountCommand::new;
    }
}
