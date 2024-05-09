package managers.data_base;

import common.model.entities.Movie;
import common.model.entities.Person;
import exceptions.DataBaseConnectionException;

import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.Vector;

public class DataBaseManager {
    private final String host;
    private final String user;
    private final String password;
    private String database = "studs";
    private int port = 5432;

    private Connection connection;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public DataBaseManager(String host, String user, String password) throws DataBaseConnectionException {
        this.host = host;
        this.user = user;
        this.password = password;

        try {
            connection = DriverManager.getConnection(host, user, password);
        } catch (SQLException e){
//            System.out.println("sqlexception");
            throw new DataBaseConnectionException(e);
        }
    }

    public DataBaseManager(String host, String user, String password, String database) throws DataBaseConnectionException {
        this(host + "/" + database, user, password);
    }

    public DataBaseManager(String host, String user, String password, String database, int port) throws DataBaseConnectionException {
        this(host + ":" + port + "/" + database, user, password);
    }

    public DataBaseManager(String filename) throws DataBaseConnectionException {
        try(Scanner file = new Scanner(new File(filename))){
            String url = "jdbc:postgresql://";
            String host = "";
            String user = "";
            String password = "";
            int port = 0;
            String database = "";

            while (file.hasNextLine()){
                String line = file.nextLine();
                String strip = line.substring(line.indexOf(":") + 1).strip();

                if(line.contains("host")){
                    host = strip;
                } else if (line.contains("port")) {
                    port = Integer.parseInt(strip);
                } else if (line.contains("database")) {
                    database = strip;
                } else if (line.contains("user")) {
                    user = strip;
                } else if (line.contains("password")) {
                    password = strip;
                }
            }

            if (host.isEmpty() || user.isEmpty() || password.isEmpty()){
                throw new DataBaseConnectionException("Файл конфигурации базы данных содержит некорректные данные");
            }

            if (!database.isEmpty()){
                this.database = database;
            }
            if (port != 0) {
                this.port = port;
            }

            if (!host.matches(".*:\\d*/.*")){
                if (host.matches(".*:\\d*")){
                    host += "/" + database;
                }
                else {
                    host += ":" + port + "/" + database;
                }
            }

            this.host = url + host;
            this.user = user;
            this.password = password;

            try {
                connection = DriverManager.getConnection(this.host, user, password);
            } catch (SQLException e){
//                System.out.println("sqlexception");
                throw new DataBaseConnectionException(e);
            }
        }
        catch (FileNotFoundException e) {
            throw new DataBaseConnectionException("Файл конфигурации базы данных не найден", e);
        }
    }

    public Vector<Movie> getMovies(){
        Vector<Movie> movies_vec = new Vector<>();

        String allMoviesQuery = "SELECT * FROM movies_prog;";
        String findDirectorQuery = "SELECT * FROM persons_prog WHERE id = ?;";
        String findUserQuery = "SELECT * FROM users_prog WHERE id = ?;";

        try {
            ResultSet movies = connection.createStatement().executeQuery(allMoviesQuery);

            PreparedStatement findDirector = connection.prepareStatement(findDirectorQuery);

            while(movies.next()){
                findDirector.setInt(1, movies.getInt("directorID"));
                ResultSet director = findDirector.executeQuery();
                director.next();

                movies_vec.add(DBModelMapper.getMovieFromDB(movies, director));

                findDirector.clearParameters();
            }

            return movies_vec;

        } catch (SQLException e) {
            System.out.println("sqlexception");
            throw new RuntimeException(e);
        }
    }

    public ResultSet makeQuery(String query){
        try {
            ResultSet result = connection.createStatement().executeQuery(query);

            return result;

        } catch (SQLException e) {
            System.out.println("sqlexception");
            throw new RuntimeException(e);
        }
    }

    public boolean insertMovie(Movie movie, int user_id){
        // вставка фильма
        String query = "INSERT INTO " +
                "movies_prog (name, coordinates, creationdate, oscarscount, goldenpalmcount, length, mpaa, directorid, creatorid) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

        // надо узнать есть ли такой режиссер в базе, если есть, то сослаться на него, если нет - вставить
        Person director = movie.getDirector();
        String directorExistsQuery = "SELECT * FROM persons_prog " +
                "WHERE name = ? AND birthdate = ? AND eyecolor = ? AND haircolor = ? AND nationality = ? AND location = ?;";

        try {
            // подготовка "запроса есть ли такой режиссер"
            PreparedStatement directorExists = connection.prepareStatement(directorExistsQuery);
            DBModelMapper.setPersonData(director, directorExists);

            // выполнения запроса на поиск режиссера
            var directorExistsResult = directorExists.executeQuery();
            directorExistsResult.next();

            // если есть, берем id
            if (directorExistsResult.getFetchSize() > 0){
                director.setId(directorExistsResult.getInt("id"));
            }
            // если нет, вставляем нового и заново выполняем подготовленный запрос на поиск
            else{
                // вставка
                String insertDirectorQuery = "" +
                        "INSERT INTO persons_prog (name, birthdate, eyecolor, haircolor, nationality, location) " +
                        "VALUES (?, ?, ?, ?, ?, ?);";
                PreparedStatement directorInsert = connection.prepareStatement(insertDirectorQuery);
                DBModelMapper.setPersonData(director, directorInsert);
                directorInsert.executeUpdate();

                // повторный поиск
                directorExistsResult = directorExists.executeQuery();
                directorExistsResult.next();
                director.setId(directorExistsResult.getInt("id"));
            }
            // режиссер готов
            // вставка фильма
            PreparedStatement insertMovieStatement = connection.prepareStatement(query);
            // назначение всех атрибутов фильма для бд
            DBModelMapper.setMovieData(movie, insertMovieStatement);
            // назначение user
            insertMovieStatement.setInt(9, user_id);
            insertMovieStatement.executeUpdate();

            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
