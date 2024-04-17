package commands;

import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import managers.ServerControlReceiver;

public abstract class AbstractServerCommand extends AbstractCommand {
    public AbstractServerCommand(String name, String description, String arguments, Object[] args) {
        super(name, description, arguments, args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        setArgs(s);
        execute(rec);
    }
    @Override
    public void execute(AbstractReceiver rec){
        return;
    }

    public abstract int execute(ServerControlReceiver rec);
}
