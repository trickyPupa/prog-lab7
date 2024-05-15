package managers;

import common.commands.abstractions.Command;

import java.util.ArrayDeque;
import java.util.Collection;

/**
 * Класс, управляющий историей команд.
 */
public class HistoryManager {
//    private ArrayDeque<Command> history;

//    public HistoryManager(){
//        history = new ArrayDeque<>();
//    }

    public static Collection<Command> next(Command c, Collection<Command> history){
        ArrayDeque<Command> queue = new ArrayDeque<>(history);

        queue.addLast(c);
        if (queue.size() > 5) queue.removeFirst();
        return queue;
    }

//    public Command getLast(){
//        return history.getLast();
//    }

//    public Collection<Command> getHistory(){
//        return history;
//    }
}
