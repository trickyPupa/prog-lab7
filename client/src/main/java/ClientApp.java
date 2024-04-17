import client.*;
import common.OutputManager;
import common.abstractions.*;
import common.exceptions.*;
import exceptions.ConnectionsFallsExcetion;
import network.ConnectionRequest;
import network.ConnectionResponse;
import network.DisconnectionRequest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.UnknownHostException;
import java.util.Arrays;

import static common.Utils.isInt;

public class ClientApp {
    public static int PORT = 1783;
    public static String HOST_NAME = "localhost";

    public static void main(String[] args) {
        if (isInt(args[1]))
            PORT = Integer.parseInt(args[1]);
        else
            System.out.println("Некорректный аргумент 1 (порт)");

        String host = args[0];
        if (host == null || host.isBlank())
            HOST_NAME = "localhost";
        else
            HOST_NAME = host;
        start();
    }

    public static void start(){
        try(InputStream input = new BufferedInputStream(System.in)){

            IInputManager inputManager = new InputManager(input);
            IOutputManager outputManager = new OutputManager();
            AbstractReceiver receiver = new ClientReceiver(inputManager, outputManager);

            AbstractClientRequestManager clientRequestManager = new ClientRequestManager(HOST_NAME, PORT);

            ClientCommandHandler handler = new ClientCommandHandler(inputManager, outputManager, clientRequestManager,
                    receiver, null);


            clientRequestManager.makeRequest(new ConnectionRequest());
            var answer = (ConnectionResponse) clientRequestManager.getResponse();
            outputManager.print(answer.getMessage());

            if (!answer.isSuccess()){
                outputManager.print("Попробуйте позже. Завершение работы.");
                System.exit(0);
            }
            handler.setCommands(answer.getCommandList());

            class MyHook extends Thread{
                private AbstractClientRequestManager crm;
                public MyHook(AbstractClientRequestManager crm) {
                    super();
                    this.crm = crm;
                }

                @Override
                public void run() {
                    if (crm != null)
                        crm.makeRequest(new DisconnectionRequest());
                }
            }

            Runtime.getRuntime().addShutdownHook(new MyHook(clientRequestManager));

            while (true){
                try {
                    handler.nextCommand();

                    // отправка серверу
                } catch (WrongArgumentException e){
                    outputManager.print(e.toString());
                } catch (InterruptException e){
                    outputManager.print("Ввод данных остановлен.");
                } catch (NoSuchCommandException e){
                    outputManager.print("Нет такой команды в доступных.");
                } catch (RecursionException e) {
                    outputManager.print("Рекурсия в исполняемом файле.");
                } catch (FileException e){
                    outputManager.print(e.getMessage());
                }
                catch (ConnectionsFallsExcetion e){
                    outputManager.print("Произошел разрыв соединения с сервером.");
                    break;
                }
                catch (RuntimeException e){
                    outputManager.print(e);
//                    System.out.println("main catch runtime");

                    clientRequestManager.makeRequest(new DisconnectionRequest());
                    receiver.exit(null);
                    throw e;
                }
            }
        }
        catch (UnknownHostException e){
            System.out.println("Адрес сервера не найден");
        }
        catch(PortUnreachableException e){
            System.out.println("Невозможно подключиться к заданному порту");
        }
        catch(IOException e){
            System.out.println("Ошибка при чтении данных");
            System.out.println(e);
            System.out.println(Arrays.toString(e.getStackTrace()));
//            System.out.println("main catch io");
//            throw new RuntimeException(e);
        }
        catch(RuntimeException e){
            System.out.println("Что-то пошло не так в ходе выполнения программы.");
//            System.out.println(e.getMessage());
            System.out.println(e);
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
