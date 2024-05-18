package managers;

import common.commands.abstractions.Command;
import common.exceptions.InterruptException;
import common.exceptions.NoSuchCommandException;
import common.exceptions.WrongArgumentException;
import common.user.*;
import common.utils.Pair;
import network.*;
import exceptions.FinishConnecton;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static common.utils.Funcs.concatBytes;

public class ServerConnectionManager {
    private final int PASSWORD_SALT_LENGTH = 64;
    private final String PASSWORD_PEPPER = "";
    private final int PACKET_SIZE = 1024;
    private final int DATA_SIZE = PACKET_SIZE - 2;
    private final Logger logger;

    protected final int port;
    private DatagramSocket socket;
    protected ServerCommandHandler handler;

    private volatile ConcurrentHashMap<SocketAddress, Pair<byte[][], Integer>> requestBuffers = new ConcurrentHashMap<>();

    private ExecutorService threadPool;
    private boolean running = true;

    public ServerConnectionManager(int p, ServerCommandHandler h) throws SocketException {
        handler = h;
        h.setServerControlReceiver(new ServerControlReceiver(this));

        port = p;
        InetSocketAddress localSocketAddress = new InetSocketAddress(port);
        socket = new DatagramSocket(localSocketAddress);

        threadPool = Executors.newCachedThreadPool();

        logger = handler.vals.logger;

        logger.info("Инициализация сервера, порт: " + port);
//        System.out.println("Старт сервера, порт: " + port);
    }

    protected Pair<byte[], SocketAddress> handlePacket(DatagramPacket packet) {
        SocketAddress address = packet.getSocketAddress();
        byte[] data = packet.getData();
        boolean received = false;

        logger.info("Получен пакет {} из {} от {}", data[PACKET_SIZE - 2] + 1, data[PACKET_SIZE - 1] + 1, address);

        /*// Инициализация буфера для нового адреса
        requestBuffers.putIfAbsent(address, new Pair<>(new byte[0], false));

        // Проверка, является ли это последний пакет в запросе
        if (data[data.length - 1] == 1) {
            received = true;
            logger.info("Получение запроса от {} окончено", address);
            data = Arrays.copyOf(data, DATA_SIZE); // Удаление байта конца
        }

        // Объединение нового чанка с существующим буфером
        byte[] existingData = requestBuffers.get(address).getFirst();
        byte[] combinedData = concatBytes(existingData, data);
        requestBuffers.put(address, new Pair<>(combinedData, received));

        if (received) {
            // удаление из буфера завершенного запроса
            requestBuffers.remove(address);
            // Обработка полного запроса
            Request request = (Request) Serializer.deserializeData(combinedData);

            logger.debug("полученные данные от {}: {}", address, request);
            logger.debug("получены данные от {}", address);

            return new Pair<>(combinedData, address);
        }

        return null;*/

        requestBuffers.putIfAbsent(address, new Pair<>(new byte[data[PACKET_SIZE - 1] + 1][DATA_SIZE], 0));

        byte[][] newData =  requestBuffers.get(address).getFirst();
        newData[data[PACKET_SIZE - 2]] = Arrays.copyOfRange(data, 0, DATA_SIZE);
        int newCount = requestBuffers.get(address).getSecond() + 1;
        requestBuffers.put(address, new Pair<>(newData, newCount));

//        logger.debug("Получено {}\nНовый набор {}\nПолучено пакетов для клиента {}",
//                Arrays.toString(data), Arrays.deepToString(newData), newCount);

        if (newCount == data[PACKET_SIZE - 1] + 1){
            received = true;
            logger.info("Получение запроса от {} окончено", address);
        }

        if (received) {
            byte[] resultData = new byte[0];
            for (var j : newData){
                resultData = concatBytes(resultData, j);
            }

            handleRequest(resultData, address);

            requestBuffers.remove(address);
        }
        return null;
    }

    protected void handleRequest(byte[] data, SocketAddress clientAddress) {
        logger.info("Начало обработки запроса от {}.", clientAddress);
        new Thread(() -> {
            Request clientData = (Request) Serializer.deserializeData(data);
            logger.debug("Получен запрос: {}", clientData);

            // запрос на соединение
            if (clientData instanceof ConnectionRequest) {
//                ((ConnectionRequest) clientData).setSuccess(true);
                sendResponse(new ConnectionResponse(ServerCommandHandler.commandsListForClient, true), clientAddress);
                logger.info("Установлено соединение с клиентом {}.", clientAddress);
            }
            // запрос на проверку логина
            else if (clientData instanceof LoginCheckRequest) {
                var loginCheckResult = loginCheck((LoginCheckRequest) clientData);
                boolean status = loginCheckResult.getFirst();
                String salt = loginCheckResult.getSecond();

                logger.info("На запрос проверки логина клиента {} получен результат {}, и будет отправлена соль {}",
                        clientAddress, status, salt);

                LoginCheckResponse response = new LoginCheckResponse("", status, salt);
                sendResponse(response, clientAddress);
                logger.info("Отправлен ответ по запросу проверки логина клиенту ", clientAddress);
            }
            // запрос на аутентификацию
            else if (clientData instanceof UserAuthRequest) {
                boolean verifying;
                if (clientData instanceof UserRegisterRequest){
                    verifying = register((UserRegisterRequest) clientData);
                }
                else{
                    verifying = authenticate((UserAuthRequest) clientData);
                }

                // история (пустая) генерируется при успешной аутентификации
                Collection<Command> history = new ArrayDeque<>();

                // отправка ответа
                var response = new UserAuthResponse("", verifying, history);
                sendResponse(response, clientAddress);
                logger.info("Отправлен ответ по запросу аутентификации пользователя клиенту ", clientAddress);
            }
            // обычный запрос на выполнение команды
            else {
                var commandRequest = (CommandRequest) clientData;
                try {
                    handler.nextCommand(commandRequest.getCommand());
                }
                catch (FinishConnecton ignored) {
                    ;
                } catch (WrongArgumentException e) {
                    logger.error("Некорректный формат команды: неправильный аргумент");
                    handler.vals.getServerOutputManager().print(e.toString());
                } catch (InterruptException e) {
                    handler.vals.getServerOutputManager().print("Ввод данных остановлен.");
                } catch (NoSuchCommandException e) {
                    logger.error("Несуществующая команда");
                    handler.vals.getServerOutputManager().print(e.getMessage());
                }
                finally {
                    /*if (clientAddress != null) {
                        String result = handler.vals.getServerOutputManager().popResponce();
                        sendResponse(new Response(result), clientAddress);
                    }*/

                    String result = handler.vals.getServerOutputManager().popResponce();
                    // историю берем из полученного запроса и добавляем к ней текущую команду
                    Collection<Command> history = HistoryManager.next(commandRequest.getCommand(), commandRequest.getHistory());
                    sendResponse(new Response(result, history), clientAddress);
                    logger.info("Отправлен ответ клиенту {}", clientAddress);
                }
            }
        }).start();
    }

    @Deprecated
    protected Pair<Request, SocketAddress> getNextRequest() throws IOException {
        boolean received = false;
        byte[] result = new byte[0];
        SocketAddress address = null;

        // получение пакетов в один запрос.
        // запрос кончается байтом "1", пока он не получится, пакеты будут суммироваться
        while(!received) {
            byte[] buf = new byte[PACKET_SIZE];
            DatagramPacket dp = new DatagramPacket(buf, PACKET_SIZE);

            socket.receive(dp);
            if (address == null){
                address = dp.getSocketAddress();
                //connect(address);
            }
            else if (!dp.getSocketAddress().equals(address)){
                logger.warn("Получен пакет от другого источника");
                //sendResponse(new ConnectionResponse(false), dp.getSocketAddress());
                continue;
            }

            logger.info("Получен пакет от {}", address);

            if (buf[buf.length - 1] == 1) {
                received = true;
                logger.info("Получение данных от {} окончено", address);
            }
            result = concatBytes(result, Arrays.copyOf(buf, buf.length - 1));
        }

        // десериализованные полученные данные
        Request data = (Request) Serializer.deserializeData(result);

        /*// отправляется ответ об успешном подключении
        if (data instanceof ConnectionRequest){

            ((ConnectionRequest) data).setSuccess(true);
            sendResponse(new ConnectionResponse(ServerCommandHandler.commandsListForClient, true), address);
//            logger.info("Установлено соединение с клиентом " + curClient);
        }
        // если сервер занят, и запрос пришел от другого клиента, ему отправляется ответ о занятости сервера
        else if (!address.equals(curClient)){
            sendResponse(new ConnectionResponse(false), address);
            logger.warn("Получен запрос от другого источника. Запрос игнорируется ");
            return null;
        }*/

        return new Pair<>(data, address);
    }

    protected void sendResponse(Response response, SocketAddress destination){
        logger.debug("Создается новый поток отправки ответа.");
        new Thread(() ->{
            byte[] buf = Serializer.prepareData(response);

            try {
                byte[][] chunks = new byte[(int) Math.ceil(buf.length / (double) DATA_SIZE)][DATA_SIZE];

                int start = 0;
                for (int i = 0; i < chunks.length; i++) {
                    chunks[i] = Arrays.copyOfRange(buf, start, start + DATA_SIZE);
                    start += DATA_SIZE;
                }

                logger.debug("Отправляются данные: \"{}\"", response.getMessage().strip());

                logger.info("Отправляется {} чанков клиенту {}", chunks.length, destination);

                for (int i = 0; i < chunks.length; i++) {
                    var chunk = chunks[i];

                    // старая версия
                    /*if (i == chunks.length - 1) {
                        DatagramPacket dp = new DatagramPacket(concatBytes(chunk, new byte[]{1}), PACKET_SIZE, destination);
                        socket.send(dp);
    //                    logger.info("Последний чанк размером " + chunk.length + " отправлен");
                    } else {
                        var dp = new DatagramPacket(concatBytes(chunk, new byte[]{0}), PACKET_SIZE, destination);
                        socket.send(dp);
    //                    logger.info("Чанк размером " + chunk.length + " отправлен");
                    }*/

                    // новая версия с количеством чанков и текущим чанком по счету
                    DatagramPacket dp = new DatagramPacket(concatBytes(chunk, new byte[]{(byte) i, (byte) (chunks.length - 1)}),
                            PACKET_SIZE, destination);

                    socket.send(dp);
                }

                logger.info("Отправка данных клиенту{} завершена", destination);

            } catch (IOException e) {
                logger.error("{}: {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                throw new RuntimeException(e);
            }
        }).start();
    }

    protected boolean authenticate(UserAuthRequest request) {
        var dbm = handler.vals.getDataBaseManager();
        var authCommand = request.getCommand();
        var user = (User) authCommand.getArgs()[1];
        String pwdDBHash;

        logger.info("Авторизация пользователя: {}", user);

        try {
            pwdDBHash = dbm.getUserPasswordHash(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return EncryptionManager.verify(user.getPassword() + PASSWORD_PEPPER, pwdDBHash);
    }

    protected boolean register(UserRegisterRequest request) {
        String salt = request.getSalt();
        var authCommand = request.getCommand();
        var user = (User) authCommand.getArgs()[1];
        var dbm = handler.vals.getDataBaseManager();

        logger.info("Регистрация пользователя: {}", user);

        String resultPwdHash = EncryptionManager.byteArrayToHexString(EncryptionManager.encrypt(user.getPassword() + PASSWORD_PEPPER));
        user.setPassword(resultPwdHash);
        try {
            if (dbm.userExists(user.getLogin()) == 0){
                dbm.insertUser(user, salt);
                return true;
            }
            else {
                return false;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Pair<Boolean, String> loginCheck(LoginCheckRequest request){
        String login = request.getLogin();
        logger.debug("Получен запрос на проверку логина {}", login);

        try {
            var dbm = handler.vals.getDataBaseManager();
            int status = dbm.userExists(login);
            // если пользователя нет, генерируем соль и сохраняем ее для текущего логина
            // если есть, берем соль из бд
            String salt;

            if (status == 0) salt = EncryptionManager.generateSalt(PASSWORD_SALT_LENGTH);
            else salt = dbm.getUserSalt(login);

            logger.debug("Ответ на запрос проверки логина {}: status = {}; salt = {}", login, status, salt);

            return new Pair<>(status != 0, salt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void disconnect(){
        logger.info("Отключение клиента {} от сервера.", socket.getRemoteSocketAddress());
        socket.disconnect();
    }

    protected void connect(SocketAddress client) throws SocketException {
        socket.connect(client);
    }

    public void start(){
        while (running) {
            try {
                byte[] buf = new byte[PACKET_SIZE];
                DatagramPacket dp = new DatagramPacket(buf, PACKET_SIZE);
                socket.receive(dp);

                threadPool.submit(() -> {
                    var pair = handlePacket(dp);

                    /*if (pair != null){
                        handleRequest(pair.getFirst(), pair.getSecond());
                    }*/
                });
            }
            catch (IOException e) {
                logger.debug("Ошибка в получении данных - {}:\n {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                return;
            }

            logger.info("Активные треды: {}", Thread.activeCount());
        }
    }

    public void close(){
        logger.info("Завершение работы сервера.");
        stop();
        threadPool.shutdown();
        socket.close();
    }

    public void stop(){
        running = false;
    }
}
