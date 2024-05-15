package common.commands.implementations;

import common.abstractions.AbstractAuthenticationReceiver;
import common.abstractions.AbstractReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class LogOutCommand extends AbstractCommand {
    public LogOutCommand(Object[] args){
        super("log_out", "Команда для выхода из аккаунта.", "no", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        ((AbstractAuthenticationReceiver) rec).logOut(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        ((AbstractAuthenticationReceiver) rec).logOut(getArgs());
    }
    @Override
    public Function<Object[], Command> getConstructor() {
        return LogOutCommand::new;
    }
}
