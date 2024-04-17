package common.commands.abstractions;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Абстрактный класс реализующие некоторые общие для всех команд аспекты: имя и описание команды.
 */
public abstract class AbstractCommand implements Command, Serializable {
    private String name;
    private String requiringArguments;
    private String description;
    private Object[] args;

//    protected AbstractCommandHandler.ShellValuables shell;

    public AbstractCommand(String name, String description, String arguments, Object[] args){
        this.name = name;
        this.requiringArguments = arguments;
        this.description = description;
//        this.shell = shell;
        setArgs(args);
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRequiringArguments() {
        return requiringArguments;
    }

    public Object[] getArgs(){
        return args;
    }
    @Override
    public void setArgs(Object[] args){
        this.args = args;
    }

    public abstract Function<Object[], Command> getConstructor();
}
