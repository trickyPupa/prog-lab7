package managers;

import common.commands.abstractions.Command;

import java.util.ArrayDeque;
import java.util.Collection;

/**
 * Класс, управляющий историей команд.
 */
public class HistoryManager {
    private ArrayDeque<Command> history;

    public HistoryManager(){
        history = new ArrayDeque<>();
    }

    public void next(Command c){
        history.addLast(c);
        if (history.size() > 5) history.removeFirst();
    }

    public Command getLast(){
        return history.getLast();
    }

    public Collection<Command> getHistory(){
        return history;
    }
}
