import common.abstractions.IOutputManager;
import common.model.entities.Coordinates;
import common.model.entities.Location;
import common.model.entities.Movie;
import common.model.entities.Person;
import common.model.enums.Country;
import common.model.enums.EyeColor;
import common.model.enums.HairColor;
import common.model.enums.MpaaRating;
import exceptions.DataBaseConnectionException;
import managers.data_base.DataBaseManager;
import managers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
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

            Movie movie = new Movie();
            movie.setName("Harry Potter 6");
            movie.setCoordinates(new Coordinates(36, 76));
            movie.setCreationDate(LocalDate.now());
            movie.setLength(123);
            movie.setGoldenPalmCount(null);
            movie.setOscarsCount(7);
            movie.setMpaaRating(MpaaRating.NC_17);

            Person person = new Person();
            person.setLocation(new Location(2, 3, 5));
            person.setBirthday(new Date(100, 10, 30));
            person.setName("Arthur");
            person.setEyeColor(EyeColor.YELLOW);
            person.setHairColor(HairColor.BLUE);
            person.setNationality(Country.FRANCE);

            movie.setDirector(person);

            try {
                db.insertMovie(movie, 1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
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
