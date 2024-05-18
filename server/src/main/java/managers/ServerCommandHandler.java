package managers;

import commands.*;
import common.abstractions.*;
import common.commands.abstractions.AbstractCommand;
import common.commands.abstractions.Command;
import common.commands.implementations.*;
import common.exceptions.NoSuchCommandException;
import common.user.User;
import managers.data_base.DataBaseManager;
import managers.data_base.PostgreDataBaseManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Класс - обработчик команд программы; считывает команды и вызывает их.
 */
public class ServerCommandHandler implements Handler{
    // класс с всеми нужными менеджерами
    public class ShellValuables {
        private ServerOutputManager serverOutputManager;
//        private final HistoryManager historyManager;
        private DataBaseManager dataBaseManager;
        private DataBaseCollectionManager dbCollectionManager;
        public final Logger logger;

        public final Map<String, Function<Object[], Command>> commands = new HashMap<>();

        public ShellValuables(ServerOutputManager out1, DataBaseCollectionManager col,
                              DataBaseManager dbManager, Logger log){
            serverOutputManager = out1;
            dbCollectionManager = col;
            dataBaseManager = dbManager;
//            historyManager = history;
            logger = log;

        }

//        public HistoryManager getHistoryManager() {
//            return historyManager;
//        }

        public ServerOutputManager getServerOutputManager() {
            return serverOutputManager;
        }

        public DataBaseManager getDataBaseManager() {
            return dataBaseManager;
        }

        public DataBaseCollectionManager getCollectionManager() {
            return dbCollectionManager;
        }
    }

    // остальные переменные

    protected ShellValuables vals;
    protected AbstractReceiver receiver;
    protected ServerControlReceiver serverControlReceiver;
    protected static Map<String, Function<Object[], AbstractServerCommand>> serverCommands = new HashMap<>();
    static {
//        serverCommands.put("disconnect", DisconnectServerCommand::new);
        serverCommands.put("stop", StopServerCommand::new);
    }
    public static final Map<String, AbstractCommand> commandsListForClient = new HashMap<>();


    public ServerCommandHandler(ServerOutputManager server_out, DataBaseCollectionManager col, PostgreDataBaseManager dbm, Logger logger){
        vals = new ShellValuables(server_out, col, dbm, logger);
        receiver = new ServerCommandReceiver(vals);

        {
            vals.commands.put("help", HelpCommand::new);
            vals.commands.put("exit", ExitCommand::new);
            vals.commands.put("add", AddCommand::new);
            vals.commands.put("show", ShowCommand::new);
            vals.commands.put("info", InfoCommand::new);
            vals.commands.put("clear", ClearCommand::new);
            vals.commands.put("update", UpdateCommand::new);
            vals.commands.put("history", HistoryCommand::new);
            vals.commands.put("remove_first", RemoveFirstCommand::new);
            vals.commands.put("remove_by_id", RemoveByIdCommand::new);
            vals.commands.put("filter_by_golden_palm_count", FilterByGoldenPalmCountCommand::new);
            vals.commands.put("min_by_coordinates", MinByCoordinatesCommand::new);
            vals.commands.put("remove_all_by_golden_palm_count", RemoveAllByGoldenPalmCountCommand::new);
            vals.commands.put("remove_lower", RemoveLowerCommand::new);
            vals.commands.put("execute_script", ExecuteScriptCommand::new);
            vals.commands.put("log_out", LogOutCommand::new);
        }

        {
            commandsListForClient.put("help", new HelpCommand(null));
            commandsListForClient.put("exit", new ExitCommand(null));
            commandsListForClient.put("add", new AddCommand(null));
            commandsListForClient.put("show", new ShowCommand(null));
            commandsListForClient.put("info", new InfoCommand(null));
            commandsListForClient.put("clear", new ClearCommand(null));
            commandsListForClient.put("update", new UpdateCommand(null));
            commandsListForClient.put("history", new HistoryCommand(null));
            commandsListForClient.put("remove_first", new RemoveFirstCommand(null));
            commandsListForClient.put("remove_by_id", new RemoveByIdCommand(null));
            commandsListForClient.put("filter_by_golden_palm_count", new FilterByGoldenPalmCountCommand(null));
            commandsListForClient.put("min_by_coordinates", new MinByCoordinatesCommand(null));
            commandsListForClient.put("remove_all_by_golden_palm_count", new RemoveAllByGoldenPalmCountCommand(null));
            commandsListForClient.put("remove_lower", new RemoveLowerCommand(null));
            commandsListForClient.put("execute_script", new ExecuteScriptCommand(null));
            commandsListForClient.put("log_out", new LogOutCommand(null));
        }
    }

    public void setServerControlReceiver(ServerControlReceiver scr){
        serverControlReceiver = scr;
    }

    public void nextCommand(Command currentCommand) {
        vals.logger.info("Выполняется команда " + currentCommand.getName());

        User user = (User) currentCommand.getArgs()[1];
        try {
            user.setId(vals.getDataBaseManager().userExists(user.getLogin()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        vals.logger.info("Переданные аргументы: " + Arrays.toString(currentCommand.getArgs()));

        currentCommand.execute(receiver);

        vals.logger.info("Команда " + currentCommand.getName() + " выполнена");
    }

    @Override
    public void nextCommand() throws IOException {
        ;
    }

    @Override
    public void nextCommand(String commandName) throws IOException {
        ;
    }

    public int nextServerCommand(String line){
        Object[] args;
        String[] split = line.strip().split(" ");
        args = Arrays.copyOfRange(split, 1, split.length);

        if (!serverCommands.containsKey(split[0])){
            vals.logger.warn("Сервер: нет такой команды.");
            return 0;
        }
        AbstractServerCommand cmd = serverCommands.get(split[0]).apply(args);
        return cmd.execute(serverControlReceiver);
    }
}
