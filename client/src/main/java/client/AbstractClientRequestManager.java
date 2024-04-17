package client;

import network.Request;
import network.Response;

import java.io.IOException;
import java.net.InetAddress;

public abstract class AbstractClientRequestManager {
    protected InetAddress address;
    protected int port;

    public AbstractClientRequestManager(InetAddress serverAddress, int p) {
        address = serverAddress;
        port = p;
    }

    public abstract void makeRequest(Request request);

    public abstract Response getResponse() throws IOException;
}
