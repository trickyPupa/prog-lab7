package client;

import common.exceptions.WrongArgumentException;
import common.user.Session;
import common.utils.Funcs;
import common.abstractions.*;
import common.commands.abstractions.AbstractCommand;
import common.exceptions.NoSuchCommandException;
import common.commands.abstractions.Command;
import common.commands.implementations.*;
import exceptions.ConnectionsFallsExcetion;
import network.*;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.function.Function;

/**
 * Класс - обработчик команд программы; считывает команды из {@link IInputManager} и провоцирует их исполнение.
 */
public class ClientCommandHandler implements Handler {

    private IInputManager inputManager;
    private IOutputManager outputManager;
    private AbstractClientRequestManager clientRequestManager;
    private AbstractAuthenticationReceiver simpleReceiver;
    private Collection<Command> history;

    public Map<String, Function<Object[], Command>> commands = new HashMap<>();
    public static Map<String, Function<Object[], Command>> authCommands = new HashMap<>();

    static {
        authCommands.put("log_in", AuthCommand::new);
        authCommands.put("sign_in", RegisterCommand::new);
        authCommands.put("exit", ExitCommand::new);
    }

    public ClientCommandHandler(IInputManager inp, IOutputManager out, AbstractClientRequestManager crm,
                                AbstractAuthenticationReceiver rec){
        inputManager = inp;
        outputManager = out;
        clientRequestManager = crm;
        simpleReceiver = rec;
    }

    public void setCommands(Map<String, AbstractCommand> cmlist){
        commands.clear();
        for (String name : cmlist.keySet()){
            commands.put(name, cmlist.get(name).getConstructor());
        }
    }

    @Override
    public void nextCommand(String line) throws IOException{
        line = line.strip();
        String commandName;
//        String[] args;
        Object[] args = {};

        if (line.contains(" ")) {
            commandName = line.substring(0, line.indexOf(" ")).strip();
            args = line.substring(1 + commandName.length()).split(" ");
        } else{
            commandName = line.strip();
//            args = new String[]{""};
        }

        if (!commands.containsKey(commandName)){
            throw new NoSuchCommandException(line);
        }
        Command currentCommand = commands.get(commandName).apply(args);
        currentCommand.setArgs(Funcs.concatObjects(new Object[]
                {currentCommand, simpleReceiver.getCurrentSession().getUser()}, currentCommand.getArgs()));

//        System.out.println(Arrays.toString(currentCommand.getArgs()));

        // выполнение команды и отправка запроса серверу

        if (currentCommand.getClass() == ExitCommand.class){
            clientRequestManager.makeRequest(new CommandRequest(currentCommand, history));
            currentCommand.execute(simpleReceiver);
        } else{
            if (currentCommand.getClass() == HistoryCommand.class){
                printHistory();
            }
            currentCommand.execute(simpleReceiver);
            clientRequestManager.makeRequest(new CommandRequest(currentCommand, history));

//            System.out.println("запрос отправлен " + currentCommand);
        }

//        System.out.println(Arrays.toString(currentCommand.getArgs()));

        // получить ответ сервера
        Response response = clientRequestManager.getResponse();
        String result = response.getMessage();
        history = response.getHistory();

        outputManager.print(result);
    }

    @Override
    public void nextCommand() throws IOException {
        if (simpleReceiver.getCurrentSession() == null){
            authenticate();

            outputManager.print("Аутентификация прошла успешно.");
            return;
        }

        outputManager.print("Введите команду:");
        String line = inputManager.nextLine().strip();

        nextCommand(line);
    }

    public void authenticate() throws IOException {
        while(true){
            outputManager.print("Необходимо войти в аккаунт для продолжения работы.");
            outputManager.print("log_in  - для входа в существующий");
            outputManager.print("sign_in - для регистрации нового");
            outputManager.print("exit - для завершения работы. Вы также можете ввести 'exit' во время авторизации/регистрации для выхода в это меню.");
            String line = inputManager.nextLine().strip();

            String commandName;

            if (line.contains(" ")) {
                commandName = line.substring(0, line.indexOf(" ")).strip();
            } else{
                commandName = line.strip();
            }

            if (!authCommands.containsKey(commandName)){
                throw new NoSuchCommandException(line);
            }
            Command currentCommand = authCommands.get(commandName).apply(new Object[] {});
            currentCommand.setArgs(Funcs.concatObjects(new Object[] {currentCommand}, currentCommand.getArgs()));

            currentCommand.execute(simpleReceiver);

            /*{//test
                System.out.println(simpleReceiver.getCurrentUser());
                var cmd = new ExitCommand(new Object[]{});
                cmd.execute(simpleReceiver);
            }//test*/

            // проверка пользователя на сервере

            boolean isLogin = currentCommand instanceof AuthCommand;
            if (isLogin){
                clientRequestManager.makeRequest(new UserAuthRequest(currentCommand));
            } else{
                clientRequestManager.makeRequest(new UserRegisterRequest(currentCommand, simpleReceiver.getCurrentSession().getSalt()));
            }

            UserAuthResponse response = (UserAuthResponse) clientRequestManager.getResponse();

            String result = response.getMessage();
            outputManager.print(result);

            if (response.getStatus()){
                history = response.getHistory();
                break;
            }
            outputManager.print("Попробуйте еще раз.");
        }
    }

    public void printHistory(){
        outputManager.print("[");
        for(Command i : history){
            outputManager.print("\t" + i.getName());
        }
        outputManager.print("]");
    }
}
