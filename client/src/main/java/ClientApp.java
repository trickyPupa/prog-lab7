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
import java.net.PortUnreachableException;
import java.net.UnknownHostException;
import java.util.Arrays;

import static common.utils.Funcs.isInt;

public class ClientApp {
    public static int PORT = 1783;
    public static String HOST_NAME = "localhost";

    public static void main(String[] args) {
//        test();
        try {
            if (isInt(args[1]))
                PORT = Integer.parseInt(args[1]);
            else
                System.out.println("Некорректный аргумент 2 (порт)");

            String host = args[0];
            if (host == null || host.isBlank())
                HOST_NAME = "localhost";
            else
                HOST_NAME = host;

            start();
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Не переданы необходимые аргументы: адрес сервера, порт.");
        }
    }

    public static void test(){
        try(InputStream input = new BufferedInputStream(System.in)){

            IInputManager inputManager = new InputManager(input);
            IOutputManager outputManager = new OutputManager();
            AbstractReceiver clientReceiver = new ClientReceiver(inputManager, outputManager);

            AbstractClientRequestManager clientRequestManager = new ClientRequestManager(HOST_NAME, PORT);
            AbstractAuthenticationReceiver receiver = new ClientAuthenticationReceiver(clientReceiver, clientRequestManager);

            ClientCommandHandler handler = new ClientCommandHandler(inputManager, outputManager, clientRequestManager,
                    receiver);

            while (true){
                try {
                    outputManager.print("Ввод:");
                    handler.nextCommand();

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
//                    clientRequestManager.makeRequest(new DisconnectionRequest());
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
        }
        catch(RuntimeException e){
            System.out.println("Что-то пошло не так в ходе выполнения программы.");
            System.out.println(e);
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public static void start(){
        try(InputStream input = new BufferedInputStream(System.in)){

            IInputManager inputManager = new InputManager(input);
            IOutputManager outputManager = new OutputManager();
            AbstractReceiver clientReceiver = new ClientReceiver(inputManager, outputManager);

            AbstractClientRequestManager clientRequestManager = new ClientRequestManager(HOST_NAME, PORT);
            AbstractAuthenticationReceiver receiver = new ClientAuthenticationReceiver(clientReceiver, clientRequestManager);

            ClientCommandHandler handler = new ClientCommandHandler(inputManager, outputManager, clientRequestManager,
                    receiver);

            // соединение с сервером
            clientRequestManager.makeRequest(new ConnectionRequest());
            var answer = (ConnectionResponse) clientRequestManager.getResponse();
            outputManager.print(answer.getMessage());

            if (!answer.isSuccess()){
                outputManager.print("Попробуйте позже. Завершение работы...");
                System.exit(0);
            }
            handler.setCommands(answer.getCommandList());
            outputManager.print("Начало работы.");

            // бесполезно
            /*{ // отключение от сервера при экстренном завершении программы
                class MyHook extends Thread {
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
            }*/

            // основной блок выполнения команд
            while (true){
                try {
                    handler.nextCommand();

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
                    System.out.println("Непредвиденная ошибка в ходе выполнения программы.");
                    outputManager.print(e);
                    outputManager.print(Arrays.toString(e.getStackTrace()));
//                    System.out.println("main catch runtime");

//                    clientRequestManager.makeRequest(new DisconnectionRequest());
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
        }
        catch(RuntimeException e){
            System.out.println("Непредвиденная ошибка в ходе выполнения программы.");
//            System.out.println(e.getMessage());
            System.out.println(e);
            e.printStackTrace();
        } finally {
            System.out.println("Завершение работы.");
        }
    }
}

// execute_script C:\Users\timof\IdeaProjects\prog-lab7\client\src\data\script.txt