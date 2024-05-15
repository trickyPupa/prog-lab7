package managers;

public class ServerControlReceiver {
    protected ServerConnectionManager serverConnectionManager;

    public ServerControlReceiver(ServerConnectionManager scm) {
        serverConnectionManager = scm;
    }

    public int disconnect(){
        serverConnectionManager.handler.vals.getServerOutputManager().print("На сервер поступила команда отключения.");
        serverConnectionManager.disconnect();
        return 0;
    }

    public int stop(){
//        disconnect();
//        serverConnectionManager.stop();
        serverConnectionManager.close();
        return 1;
    }
}
