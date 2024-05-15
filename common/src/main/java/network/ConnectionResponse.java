package network;

import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConnectionResponse extends Response{
    protected boolean success;

    protected Map<String, AbstractCommand> commandList;

    public ConnectionResponse(String msg, boolean success) {
        super(msg, null);
        this.success = success;
    }

    public ConnectionResponse(Map<String, AbstractCommand> cmlist, boolean success) {
        this(success);

        commandList = cmlist;
    }

    public ConnectionResponse(boolean success) {
        super(success ? "Успешное подключение к серверу." : "Подключение к серверу недоступно в данный момент.", null);
        this.success = success;
    }

    public boolean isSuccess(){
        return success;
    }

    public Map<String, AbstractCommand> getCommandList(){
        return commandList;
    }
}
