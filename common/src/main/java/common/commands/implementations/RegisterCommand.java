package common.commands.implementations;

import common.abstractions.AbstractReceiver;
import common.abstractions.AbstractAuthenticationReceiver;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import java.util.function.Function;

public class RegisterCommand extends AbstractCommand {
    public RegisterCommand(Object[] args){
        super("sign_in", "Команда для регистрации нового пользователя.", "{user}", args);
    }

    @Override
    public void execute(String[] s, AbstractReceiver rec) {
        ((AbstractAuthenticationReceiver) rec).registerUser(s);
    }
    @Override
    public void execute(AbstractReceiver rec) {
        ((AbstractAuthenticationReceiver) rec).registerUser(getArgs());
    }
    @Override
    public Function<Object[], Command> getConstructor() {
        return RegisterCommand::new;
    }
}
