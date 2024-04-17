package managers;

import common.exceptions.InterruptException;
import common.exceptions.NoSuchCommandException;
import common.exceptions.WrongArgumentException;
import network.*;
import exceptions.FinishConnecton;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import static common.Utils.concatBytes;

public class ServerConnectionManager {
    private final int PACKET_SIZE = 1024;
    private final int DATA_SIZE = PACKET_SIZE - 1;
    private final Logger logger;

    protected final int port;
    private InetSocketAddress localSocketAddress;
    private SocketAddress curClient = null;
    private DatagramSocket socket;
    protected ServerCommandHandler handler;

    public ServerConnectionManager(int p, ServerCommandHandler h) throws SocketException {
        handler = h;
        h.setServerControlReceiver(new ServerControlReceiver(this));

        port = p;
        localSocketAddress = new InetSocketAddress(port);
        socket = new DatagramSocket(localSocketAddress);

        logger = handler.vals.logger;

        logger.info("Инициализация сервера, порт: " + port);
//        System.out.println("Старт сервера, порт: " + port);
    }

    protected Request getNextRequest() throws IOException {
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
            } else if (!dp.getSocketAddress().equals(address)){
                logger.warn("Получен пакет от другого источника");
                sendResponse(new ConnectionResponse(false), dp.getSocketAddress());
                continue;
            }

            logger.info("Получен пакет от " + address);

            if (buf[buf.length - 1] == 1){
                received = true;
                logger.info("Получение данных от " + address + " окончено");
            }
            result = concatBytes(result, Arrays.copyOf(buf, buf.length - 1));
        }

        Request data = (Request) Serializer.deserializeData(result); // полученные данные

        // если сейчас сервер не занят, и новый запрос - это запрос о подключении, то этот клиент устанавливается текущим;
        // отправляется ответ об успешном подключении
        if (curClient == null && data instanceof ConnectionRequest){

            curClient = address;
            ((ConnectionRequest) data).setSuccess(true);
            sendResponse(new ConnectionResponse(ServerCommandHandler.commandsListForClient, true), address);
//            logger.info("Установлено подключение с клиентом " + curClient);

            return data;
        }
        // если сервер занят, и запрос пришел от другого клиента, ему отправляется ответ о занятости сервера
        else if (!address.equals(curClient)){
            sendResponse(new ConnectionResponse(false), address);
            logger.warn("Получен запрос от другого источника. Запрос игнорируется ");
            return null;
        }

        return data;
    }

    protected void sendResponse(Response response, SocketAddress destination){
        byte[] buf = Serializer.prepareData(response);

        try {
            byte[][] chunks = new byte[(int) Math.ceil(buf.length / (double) DATA_SIZE)][DATA_SIZE];

            int start = 0;
            for (int i = 0; i < chunks.length; i++) {
                chunks[i] = Arrays.copyOfRange(buf, start, start + DATA_SIZE);
                start += DATA_SIZE;
            }

            logger.info("Отправляются данные: \"" + response.getMessage().strip() + "\"");

            logger.info("Отправляется " + chunks.length + " чанков клиенту " + destination);

            for (int i = 0; i < chunks.length; i++) {
                var chunk = chunks[i];
                if (i == chunks.length - 1) {
                    DatagramPacket dp = new DatagramPacket(concatBytes(chunk, new byte[]{1}), PACKET_SIZE, destination);
                    socket.send(dp);
//                    logger.info("Последний чанк размером " + chunk.length + " отправлен");
                } else {
                    var dp = new DatagramPacket(concatBytes(chunk, new byte[]{0}), PACKET_SIZE, destination);
                    socket.send(dp);
//                    logger.info("Чанк размером " + chunk.length + " отправлен");
                }
            }

            logger.info("Отправка данных клиенту" + destination + " завершена");

        } catch (IOException e){
            logger.error(e.getMessage() +  ": " + Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    protected void disconnect(){
        if (curClient != null) {
            sendResponse(new ConnectionResponse(false), curClient);

            handler.vals.getServerOutputManager().print("Конец работы. Отключение от сервера.");
            curClient = null;

            logger.info("Отключение клиента от сервера.");
        }
    }

    public void run(){
        Request request;
        // в бесконечном цикле принимаем подключения, если нашли, прорабатываем его до конца
        try {
            if ((request = getNextRequest()) == null){
                return;
            } else if (request instanceof ConnectionRequest && ((ConnectionRequest) request).isSuccess()) {
                logger.info("Подключение клиента к серверу, адрес: " + curClient);
            } else if (request instanceof DisconnectionRequest && curClient != null) {
                disconnect();
            } else {
                try {
                    handler.nextCommand(((CommandRequest) request).getCommand());

                    handler.receiver.save(null);

//                    String result = handler.vals.getServerOutputManager().popResponce();
//                    sendResponse(new Response(result), curClient);

                } catch (FinishConnecton e){
                    disconnect();
                } catch (WrongArgumentException e){
                    logger.error("Некорректный формат команды: неправильный аргумент");
                    handler.vals.getServerOutputManager().print(e.toString());
                } catch (InterruptException e){
                    handler.vals.getServerOutputManager().print("Ввод данных остановлен.");
                } catch (NoSuchCommandException e) {
                    logger.error("Несуществующая команда");
                    handler.vals.getServerOutputManager().print(e.getMessage());
                } finally {
                    if (curClient != null) {
                        String result = handler.vals.getServerOutputManager().popResponce();
                        sendResponse(new Response(result), curClient);
                    }
                }
            }
        } catch (SocketException e){

        } catch (IOException e){
            if (curClient != null) {
                handler.vals.getServerOutputManager().print("Конец работы из-за ошибки [" + e + "] на сервере. Отключение от сервера.");
                String result = handler.vals.getServerOutputManager().popResponce();
                sendResponse(new Response(result), curClient);
            }

            logger.error(e.getMessage() +  ": " + Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    public void close(){
        disconnect();
        socket.close();
    }
}
