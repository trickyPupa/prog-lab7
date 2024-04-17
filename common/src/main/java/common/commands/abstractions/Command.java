package common.commands.abstractions;

import common.abstractions.AbstractReceiver;

import java.io.Serializable;

/**
 * Родительский интерфейс для всех команд.
 */
public interface Command extends Serializable {
    /**
     * @return name - имя команды
     */
    String getName();

    /**
     * Исполнение логики команды.
     * @param s - аргументы, передаваемые команде.
     */
    void execute(String[] s, AbstractReceiver rec);
    void execute(AbstractReceiver rec);
    void setArgs(Object[] args);
    Object[] getArgs();
}
