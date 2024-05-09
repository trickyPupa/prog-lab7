import common.abstractions.IOutputManager;
import exceptions.DataBaseConnectionException;
import managers.data_base.DataBaseManager;
import managers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketException;
import java.util.Arrays;
import java.util.Scanner;

public class ServerApp {

    public static int PORT = 1783;
    public static Logger logger = LogManager.getRootLogger();

    public static void main(String[] args) {
//        String filename = args[0].strip();
//        start(filename);

        test();
    }

    public static void test(){
        try {
            var db = new DataBaseManager("C:\\Users\\timof\\IdeaProjects\\prog-lab7\\server\\src\\main\\resources\\config.txt");
//            var db = new DataBaseManager("jdbc:postgresql://localhost:5432/prog", "postgres", "qwer");

            String q1 = "select * from persons_prog";
            String q2 = "select * from movies_prog";
            var a = db.getMovies();
            for (common.model.entities.Movie movie : a) {
                System.out.println(movie);
            }

        }
        catch (DataBaseConnectionException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    private static void start(String filename){
        IOutputManager outputManager = new ServerOutputManager();
        FileManager fileManager = new FileManager(filename);
        CollectionManager collectionManager = new CollectionManager(fileManager.collectionFromFile());

        ServerCommandHandler handler = new ServerCommandHandler((ServerOutputManager) outputManager, collectionManager,
                fileManager, logger);

        ServerConnectionManager serverConnectionManager = null;
        while (serverConnectionManager == null) {
            try {
                serverConnectionManager = new ServerConnectionManager(PORT, handler);
                logger.info("Начало работы сервера");
            } catch (SocketException e) {
                logger.error("Невозможно подключиться к порту " + PORT);
                PORT++;
            }
        }

        Scanner scanner = new Scanner(System.in);

        class NonblockInput extends Thread {
            private ServerConnectionManager scm;
            private Scanner scanner;

            public NonblockInput(ServerConnectionManager s, Scanner a) {
                super();
                scm = s;
                scanner = a;
            }

            public void run() {
                while (true) {
                    if (scanner.hasNext()){
                        String text = scanner.nextLine();

                        int commandCode = handler.nextServerCommand(text);
                        if (commandCode == 1){
                            logger.info("Завершение работы сервера.");
                            scm.close();
                            scanner.close();
                            System.exit(0);
                        }

                    }
                }
            }
        }

        NonblockInput a = new NonblockInput(serverConnectionManager, scanner);
        a.setPriority(Thread.MIN_PRIORITY);
        a.start();

        while (true) {
            try {
                serverConnectionManager.run();

            }
            catch (RuntimeException e) {
//                System.out.println(e);
//                System.out.println("main catch runtime");
                logger.error(e + "\n" + Arrays.toString(e.getStackTrace()));
            }
        }
    }
}
