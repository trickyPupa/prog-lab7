package common.abstractions;

import java.io.IOException;

public interface Handler {
    /**
     * Пытается считать и выполнить следующую команду.
     */
    public void nextCommand() throws IOException;
    /**
     * Выполняет командуЮ данную в качестве параметра.
     * @param commandName - название команды
     */
    public void nextCommand(String commandName) throws IOException;
}
