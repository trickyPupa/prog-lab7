package client;

import network.ConnectionResponse;
import network.Request;
import network.Response;
import network.Serializer;
import exceptions.ConnectionsFallsExcetion;

import static common.utils.Funcs.concatBytes;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;

public class ClientRequestManager extends AbstractClientRequestManager {
    private final int PACKET_SIZE = 1024;
    private final int DATA_SIZE = PACKET_SIZE - 2;

    private DatagramSocket socket;
    private SocketAddress socketAddress;
    private DatagramChannel channel;

    public ClientRequestManager(String serverName, int port) throws UnknownHostException {
        this(InetAddress.getByName(serverName), port);
    }

    public ClientRequestManager(byte[] ip, int port) throws UnknownHostException {
        this(InetAddress.getByAddress(ip), port);
    }

    public ClientRequestManager(InetAddress address, int port){
        super(address, port);

        try {
            socket = new DatagramSocket();
            socketAddress = new InetSocketAddress(address, port);
            socket.connect(socketAddress);

            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.connect(socketAddress);

        } catch (SocketException e) {
            throw new ConnectionsFallsExcetion(e.getMessage());
        } catch (IOException e){
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
        } catch (RuntimeException e){
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void makeRequest(Request request) {
        byte[] preparedData = Serializer.prepareData(request);

        byte[][] chunks = new byte[(int)Math.ceil(preparedData.length / (double)DATA_SIZE)][DATA_SIZE];

        int start = 0;
        for(int i = 0; i < chunks.length; i++) {
            chunks[i] = Arrays.copyOfRange(preparedData, start, start + DATA_SIZE);
            start += DATA_SIZE;
        }

        try {
            for(int i = 0; i < chunks.length; i++) {
                var chunk = chunks[i];

                // старая версия
                /*if (i == chunks.length - 1) {
                    byte[] lastChunk = concatBytes(chunk, new byte[]{(byte) i, (byte) (chunks.length - 1)});

                    channel.send(ByteBuffer.wrap(lastChunk), socketAddress);

//                    System.out.println("Отправлен последний чанк из " + chunks.length);
                } else {
                    byte[] res = concatBytes(chunk, new byte[]{0});

                    channel.send(ByteBuffer.wrap(res), socketAddress);

//                    System.out.println("Отправлен " + i + " чанк из " + chunks.length);
                }*/

                // новая версия
                byte[] packet = concatBytes(chunk, new byte[]{(byte) i, (byte) (chunks.length - 1)});
                channel.send(ByteBuffer.wrap(packet), socketAddress);
            }

//            ByteBuffer buf = ByteBuffer.allocate(preparedData.length);
//            buf.put(preparedData);

        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Response getResponse() throws IOException{
        boolean received = false;
        byte[][] data = null;
        Integer chunksCount = null;
        int curCount = 0;

        while (!received) {
            ByteBuffer buf = ByteBuffer.allocate(PACKET_SIZE);
            SocketAddress addr = null;

            while (addr == null)
                addr = channel.receive(buf);
            curCount++;

            byte[] bufArray = buf.array();

            if (chunksCount == null){
                chunksCount = bufArray[PACKET_SIZE - 1] + 1;
                data = new byte[chunksCount][DATA_SIZE];
            }

            /*if (buf.array()[PACKET_SIZE - 1] == 1) {
                received = true;
            }*/

            if (curCount == chunksCount){
                received = true;
            }

//            data = concatBytes(data, Arrays.copyOf(buf.array(), DATA_SIZE));
            data[bufArray[PACKET_SIZE - 2]] = Arrays.copyOf(bufArray, DATA_SIZE);
        }
        byte[] resultData = new byte[0];
        for (var j : data){
            resultData = concatBytes(resultData, j);
        }

        Response cr = (Response) Serializer.deserializeData(resultData);

        if (cr instanceof ConnectionResponse && !((ConnectionResponse) cr).isSuccess()){
            throw new ConnectionsFallsExcetion();
        }

        return cr;
    }
}
