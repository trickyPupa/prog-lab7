package managers.data_base;

import common.model.entities.Movie;
import exceptions.DataBaseConnectionException;

import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class PostgreDataBaseManager extends DataBaseManager{


    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public PostgreDataBaseManager(String url, String user, String password) throws DataBaseConnectionException {
        super(url, user, password);
    }

    public PostgreDataBaseManager(String host, String user, String password, String database) throws DataBaseConnectionException {
        this(host + "/" + database, user, password);
    }

    public PostgreDataBaseManager(String host, String user, String password, String database, int port) throws DataBaseConnectionException {
//        this(host + ":" + port + "/" + database, user, password);
        super("postgresql", host, user, password, database, port);
    }

    public PostgreDataBaseManager(String filename) throws DataBaseConnectionException {
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

            if (!host.matches(".*:\\d*/.*")){
                if (host.matches(".*:\\d*")){
                    host += "/" + database;
                }
                else {
                    host += ":" + port + "/" + database;
                }
            }

            this.url = url + host;
            this.user = user;
            this.password = password;

            connect(url + host, user, password);
        }
        catch (FileNotFoundException e) {
            throw new DataBaseConnectionException("Файл конфигурации базы данных не найден", e);
        }
    }

    @Override
    protected void initQueries(){
        findMovieByIdQuery = "SELECT * FROM movies_prog WHERE id = ?;";
        findPersonByIdQuery = "SELECT * FROM persons_prog WHERE id = ?;";
        findUserByIdQuery = "SELECT login FROM users_prog WHERE id = ?;";
        findPersonQuery = "SELECT * FROM persons_prog " +
                "WHERE name = ? AND birthdate = ? AND eyecolor = ? AND haircolor = ? AND nationality = ? AND location = ?;";
        findUserQuery = "SELECT id FROM users_prog WHERE login = ?;";

        insertMovieQuery = "INSERT INTO " +
                "movies_prog (name, coords, oscarscount, goldenpalmcount, length, mpaa, director_id, creator_id) " +
                "VALUES (?, ROW(?, ?), ?, ?, ?, ?, ?, ?);";;
        insertPersonQuery = "INSERT INTO persons_prog (name, birthdate, eyecolor, haircolor, nationality, location) " +
                        "VALUES (?, ?, ?, ?, ?, ?);";
        insertUserQuery = "INSERT INTO users_prog (login, password_hash, salt) VALUES (?, ?, ?);";
        deleteMovieQuery = "DELETE FROM movies_prog WHERE id = ?;";
        updateMovieQuery = "UPDATE movies_prog " +
                "SET (name, coords, oscarscount, goldenpalmcount, length, mpaa, director_id) " +
                "= (?, ROW(?, ?), ?, ?, ?, ?, ?)" +
                "WHERE id = ?;";

        getUserSaltQuery = "SELECT salt FROM users_prog WHERE login = ?;";
        getUserPasswordHashQuery = "SELECT password_hash FROM users_prog WHERE login = ?;";
    }

    @Override
    protected void connect(String url, String user, String password) throws DataBaseConnectionException {
        try {
            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false);

//            System.out.println(makeQuery("SELECT 1").next());
        } catch (SQLException e){
//                System.out.println("sqlexception");
            throw new DataBaseConnectionException(e);
        }
    }

    @Override
    public CopyOnWriteArrayList<Movie> getMovies(){
        CopyOnWriteArrayList<Movie> movies_vec = new CopyOnWriteArrayList<>();

        String allMoviesQuery = "SELECT * FROM movies_prog;";
        String findDirectorQuery = "SELECT * FROM persons_prog WHERE id = ?;";

        try {
            ResultSet movies = connection.createStatement().executeQuery(allMoviesQuery);

            PreparedStatement findDirector = connection.prepareStatement(findDirectorQuery);

            while(movies.next()){
                findDirector.setInt(1, movies.getInt("director_id"));
                ResultSet director = findDirector.executeQuery();
                director.next();

                String userLogin = findUser(movies.getInt("creator_id"));

                movies_vec.add(DBModelMapper.getMovieFromDB(movies, director, userLogin));

                findDirector.clearParameters();
            }

            /*for (var i : movies_vec){
                System.out.println(i);
            }*/

            return movies_vec;

        } catch (SQLException e) {
            System.out.println("sqlexception");
            throw new RuntimeException(e);
        }
    }

//    public Movie getMovieByID(int id) throws SQLException {
//        String findMovieById = "SELECT * FROM movies_prog WHERE id = ?;";
//        String findDirectorQuery = "SELECT * FROM persons_prog WHERE id = ?;";
//
//        PreparedStatement movieStatement = connection.prepareStatement(findMovieById);
//        movieStatement.setInt(1, id);
//
//        ResultSet movie = movieStatement.executeQuery();
//
//        PreparedStatement findDirector = connection.prepareStatement(findDirectorQuery);
//
//        if (movie.next()) {
//            findDirector.setInt(1, movie.getInt("director_id"));
//            ResultSet director = findDirector.executeQuery();
//            director.next();
//
//            String userLogin = findUser(movie.getInt("creator_id"));
//
//            return DBModelMapper.getMovieFromDB(movie, director, userLogin);
//        } else{
//            return null;
//        }
//    }
//
//    public ResultSet makeQuery(String query){
//        try {
//            ResultSet result = connection.createStatement().executeQuery(query);
//
//            return result;
//
//        } catch (SQLException e) {
//            System.out.println("sqlexception");
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void insertMovie(Movie movie, int user_id) throws SQLException {
//        // точка сохранения
////        Savepoint lastOKSavePoint = connection.setSavepoint();
//        connection.commit();
//
//        // вставка фильма
//        String query = "INSERT INTO " +
//                "movies_prog (name, coordinates_x, coordinates_y, oscarscount, goldenpalmcount, length, mpaa, director_id, creator_id) " +
//                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
//
//        // надо узнать есть ли такой режиссер в базе, если есть, то сослаться на него, если нет - вставить
//        Person director = movie.getDirector();
//
//        try{
//            getOrInsertPerson(director);
//        } catch (SQLException e) {
////            connection.rollback();
//            throw new SQLException("Не удалось вставить режиссера.");
//        }
//
//        try {
//            // вставка фильма
//            PreparedStatement insertMovieStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
//            // назначение всех атрибутов фильма для бд
//            DBModelMapper.setMovieData(movie, insertMovieStatement);
//            // назначение user
//            insertMovieStatement.setInt(9, user_id);
//            insertMovieStatement.executeUpdate();
//
//            // установка новому фильму генерируемых значений
//            var generatedKeys = insertMovieStatement.getGeneratedKeys();
//
//            if (generatedKeys.next()) {
//                int movieId = generatedKeys.getInt(1);
//                movie.setId(movieId);
//
//                movie.setCreator(findUser(user_id));
//
//                movie.setCreationDate(getMovieByID(movieId).getCreationDate());
//            } else {
//                throw new SQLException("Не удалось получить авто сгенерированные данные фильма");
//            }
//
//            connection.commit();
//
//        } catch (SQLException e) {
////            connection.rollback(lastOKSavePoint);
//            connection.rollback();
//            throw new SQLDataInsertingException("inserting movie", e);
//        }
//    }
//
//    public void deleteMovie(Movie movie, int user_id) throws SQLException, WrongUserException {
//        // проверка пользователя, тот ли создатель
//        if (userExists(movie.getCreator()) != user_id){
//            throw new WrongUserException();
//        }
//
//        // точка сохранения
//        Savepoint lastOKSavePoint = connection.setSavepoint();
//
//        // запрос удаления фильма
//        String query = "DELETE FROM movies_prog WHERE id = ?;";
//
//        try {
//            PreparedStatement deleteMovieStatement = connection.prepareStatement(query);
//            deleteMovieStatement.setInt(1, movie.getId());
//            deleteMovieStatement.executeUpdate();
//
//            connection.commit();
//
//        } catch (SQLException e) {
//            connection.rollback(lastOKSavePoint);
//            throw new SQLDataInsertingException("deleting movie", e);
//        }
//    }
//
//    public void updateMovie(Movie movie, int user_id) throws SQLException, WrongUserException {
//        // проверка пользователя: тот ли создатель
//        if (!Objects.equals(movie.getCreator(), findUser(user_id))){
//            throw new WrongUserException();
//        }
//
//        // точка сохранения
//        Savepoint lastOKSavePoint = connection.setSavepoint();
//
//        // вставка фильма
//        String query = "UPDATE movies_prog " +
//                "SET (name, coordinates_x, coordinates_y, oscarscount, goldenpalmcount, length, mpaa, director_id) " +
//                "= (?, ?, ?, ?, ?, ?, ?, ?)" +
//                "WHERE id = ?;";
//
//        // надо узнать есть ли такой режиссер в базе, если есть, то сослаться на него, если нет - вставить
//        Person director = movie.getDirector();
//
//        try {
//            getOrInsertPerson(director);
//
//            // вставка фильма
//            PreparedStatement updateMovieStatement = connection.prepareStatement(query);
//            // назначение всех атрибутов фильма для бд
//            DBModelMapper.setMovieData(movie, updateMovieStatement);
//            updateMovieStatement.setInt(9, movie.getId());
//            updateMovieStatement.executeUpdate();
//
//            connection.commit();
//
//        } catch (SQLException e) {
//            connection.rollback(lastOKSavePoint);
//            throw new SQLDataInsertingException("inserting movie", e);
//        }
//    }
//
//    protected int personExists(Person director) throws SQLException {
//        String query = "SELECT * FROM persons_prog " +
//                "WHERE name = ? AND birthdate = ? AND eyecolor = ? AND haircolor = ? AND nationality = ? AND location = ?;";
//
//        PreparedStatement directorExists = connection.prepareStatement(query);
//        DBModelMapper.setPersonData(director, directorExists);
//        ResultSet result = directorExists.executeQuery();
//        if (result.next()) {
//            return result.getInt("id");
//        } else{
//            return 0;
//        }
//    }
//
//    protected void insertPerson(Person person) throws SQLException {
//        String insertDirectorQuery =
//                "INSERT INTO persons_prog (name, birthdate, eyecolor, haircolor, nationality, location) " +
//                        "VALUES (?, ?, ?, ?, ?, ?);";
//        try {
//            PreparedStatement directorInsert = connection.prepareStatement(insertDirectorQuery, Statement.RETURN_GENERATED_KEYS);
//            DBModelMapper.setPersonData(person, directorInsert);
//            int a = directorInsert.executeUpdate();
//
//            if (a == 0){
//                throw new SQLException();
//            } else {
//                ResultSet generatedKeys = directorInsert.getGeneratedKeys();
//                if (generatedKeys.next()) {
//                    person.setId(generatedKeys.getInt(1));
//                } else {
//                    throw new SQLException("Не удалось получить id созданного режиссера.");
//                }
//            }
//
////            connection.commit();
//        } catch (SQLException e) {
//            throw new SQLDataInsertingException("inserting person", e);
//        }
//
//    }
//
//    protected void getOrInsertPerson(Person person) throws SQLException {
//        int isDirector = personExists(person);
//        if (isDirector == 0) {
//            // Если режиссера нет, вставляем его в базу данных
//            try {
//                insertPerson(person);
//            } catch (SQLDataInsertingException e) {
////                    connection.rollback(lastOKSavePoint);
//                throw e;
//            }
//        }
//        else {
//            person.setId(isDirector);
//        }
//    }
//
//    /**
//     * метод проверяет существование пользователя с таким логином в базе данных
//     * @param login
//     * @return 0 - если пользователь не найден; id пользователя, если такой есть в базе
//     * @throws SQLException
//     */
//    public int userExists(String login) throws SQLException {
//        String query = "SELECT id FROM users_prog WHERE login = ?;";
//
//        PreparedStatement userExists = connection.prepareStatement(query);
//        userExists.setString(1, login);
//
//        ResultSet result = userExists.executeQuery();
//        if (result.next()) {
//            return result.getInt("id");
//        } else{
//            return 0;
//        }
//    }
//
//    public String findUser(int id) throws SQLException {
//        String query = "SELECT login FROM users_prog WHERE id = ?;";
//
//        PreparedStatement userExists = connection.prepareStatement(query);
//        userExists.setInt(1, id);
//
//        ResultSet result = userExists.executeQuery();
//        if (result.next()) {
//            return result.getString("login");
//        } else{
//            return null;
//        }
//    }
//
//    public void insertUser(User user, String salt) throws SQLException {
//        int id = userExists(user.getLogin());
//
//        if (id != 0){
//            user.setId(id);
//        } else{
//
//            try {
//                String userInsertQuery = "INSERT INTO users_prog (login, password_hash, salt) VALUES (?, ?, ?)";
//
//                PreparedStatement userInsert = connection.prepareStatement(userInsertQuery, Statement.RETURN_GENERATED_KEYS);
//                userInsert.setString(1, user.getLogin());
//                userInsert.setString(2, user.getPassword());
//                userInsert.setString(3, salt);
//
//                int a = userInsert.executeUpdate();
//
//                if (a == 0){
//                    throw new SQLException();
//                } else {
//                    ResultSet generatedKeys = userInsert.getGeneratedKeys();
//                    if (generatedKeys.next()) {
//                        user.setId(generatedKeys.getInt(1));
//                    } else {
//                        throw new SQLException("Не удалось получить id созданного пользователя.");
//                    }
//                }
//                connection.commit();
//            } catch (SQLException e) {
//                throw new SQLDataInsertingException("inserting user", e);
//            }
//        }
//
//    }
//
//    public String getUserSalt(String login) throws SQLException {
//        String query = "SELECT salt FROM users_prog WHERE login = ?;";
//
//        PreparedStatement userSalt = connection.prepareStatement(query);
//        userSalt.setString(1, login);
//
//        ResultSet result = userSalt.executeQuery();
//        if (result.next()) {
//            return result.getString("salt");
//        } else{
//            return null;
//        }
//    }
//
//    public String getUserPasswordHash(User user) throws SQLException {
//        String query = "SELECT password_hash FROM users_prog WHERE login = ?;";
//
//        PreparedStatement userPwd = connection.prepareStatement(query);
//        userPwd.setString(1, user.getLogin());
//
//        ResultSet result = userPwd.executeQuery();
//        if (result.next()) {
//            return result.getString("password_hash");
//        } else{
//            return null;
//        }
//    }
}
