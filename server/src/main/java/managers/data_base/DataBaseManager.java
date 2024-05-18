package managers.data_base;

import common.model.entities.Movie;
import common.model.entities.Person;
import common.user.User;
import exceptions.DataBaseConnectionException;
import exceptions.SQLDataInsertingException;
import exceptions.WrongUserException;

import java.sql.*;
import java.util.Collection;
import java.util.Objects;

public abstract class DataBaseManager {
    private String driver;
    protected String user;
    protected String password;
    protected String url;

    protected Connection connection = null;

    protected String findMovieByIdQuery;
    protected String findPersonByIdQuery;
    protected String findUserByIdQuery;
    protected String findPersonQuery;
    protected String findUserQuery;

    protected String insertMovieQuery;
    protected String insertPersonQuery;
    protected String insertUserQuery;
    protected String deleteMovieQuery;
    protected String updateMovieQuery;

    protected String getUserSaltQuery;
    protected String getUserPasswordHashQuery;


    public DataBaseManager(String driver, String host, String user, String password, String database, int port) throws DataBaseConnectionException {
        this.driver = driver;
        this.user = user;
        this.password = password;

        url = "jdbc:" + driver + "://" + host + ":" + port + "/" + database;

        connect(url, user, password);
        initQueries();
    }

    public DataBaseManager(String url, String user, String password) throws DataBaseConnectionException {
        this.url = url;
        this.user = user;
        this.password = password;

        connect(url, user, password);
        initQueries();
    }

    public DataBaseManager() {
        initQueries();
    }

    protected abstract void connect(String url, String user, String password) throws DataBaseConnectionException;

    protected abstract void initQueries();

    public abstract Collection<Movie> getMovies();

    public Movie getMovieByID(int id) throws SQLException {
        PreparedStatement movieStatement = connection.prepareStatement(findMovieByIdQuery);
        movieStatement.setInt(1, id);

        ResultSet movie = movieStatement.executeQuery();

        PreparedStatement findDirector = connection.prepareStatement(findPersonByIdQuery);

        if (movie.next()) {
            findDirector.setInt(1, movie.getInt("director_id"));
            ResultSet director = findDirector.executeQuery();
            director.next();

            String userLogin = findUser(movie.getInt("creator_id"));

            return DBModelMapper.getMovieFromDB(movie, director, userLogin);
        } else{
            return null;
        }
    }

    public ResultSet makeQuery(String query) throws SQLException {
        ResultSet result = connection.createStatement().executeQuery(query);

        return result;
    }

    public void insertMovie(Movie movie, int user_id) throws SQLException {
        // точка сохранения
//        Savepoint lastOKSavePoint = connection.setSavepoint();
        connection.commit();

        // надо узнать есть ли такой режиссер в базе, если есть, то сослаться на него, если нет - вставить
        Person director = movie.getDirector();

        try{
            getOrInsertPerson(director);
        } catch (SQLException e) {
//            connection.rollback();
            throw new SQLException("Не удалось вставить режиссера.");
        }

        try {
            // вставка фильма
            PreparedStatement insertMovieStatement = connection.prepareStatement(insertMovieQuery, Statement.RETURN_GENERATED_KEYS);
            // назначение всех атрибутов фильма для бд
            DBModelMapper.setMovieData(movie, insertMovieStatement);
            // назначение user
            insertMovieStatement.setInt(9, user_id);
            insertMovieStatement.executeUpdate();

            // установка новому фильму генерируемых значений
            var generatedKeys = insertMovieStatement.getGeneratedKeys();

            if (generatedKeys.next()) {
                int movieId = generatedKeys.getInt(1);
                movie.setId(movieId);

                movie.setCreator(findUser(user_id));

                movie.setCreationDate(getMovieByID(movieId).getCreationDate());
            } else {
                throw new SQLException("Не удалось получить авто сгенерированные данные фильма");
            }

            connection.commit();

        } catch (SQLException e) {
//            connection.rollback(lastOKSavePoint);
            connection.rollback();
            throw new SQLDataInsertingException("inserting movie", e);
        }
    }

    public void deleteMovie(Movie movie, int user_id) throws SQLException, WrongUserException {
        // проверка пользователя, тот ли создатель
        if (userExists(movie.getCreator()) != user_id){
            throw new WrongUserException();
        }

        // точка сохранения
        Savepoint lastOKSavePoint = connection.setSavepoint();

        try {
            PreparedStatement deleteMovieStatement = connection.prepareStatement(deleteMovieQuery);
            deleteMovieStatement.setInt(1, movie.getId());
            deleteMovieStatement.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            connection.rollback(lastOKSavePoint);
            throw new SQLDataInsertingException("deleting movie", e);
        }
    }

    public void updateMovie(Movie movie, int user_id) throws SQLException, WrongUserException {
        // проверка пользователя: тот ли создатель
        if (!Objects.equals(movie.getCreator(), findUser(user_id))){
            throw new WrongUserException();
        }

        // точка сохранения
        Savepoint lastOKSavePoint = connection.setSavepoint();

        // надо узнать есть ли такой режиссер в базе, если есть, то сослаться на него, если нет - вставить
        Person director = movie.getDirector();

        try {
            getOrInsertPerson(director);

            // вставка фильма
            PreparedStatement updateMovieStatement = connection.prepareStatement(updateMovieQuery);
            // назначение всех атрибутов фильма для бд
            DBModelMapper.setMovieData(movie, updateMovieStatement);
            updateMovieStatement.setInt(9, movie.getId());
            updateMovieStatement.executeUpdate();

            connection.commit();

        } catch (SQLException e) {
            connection.rollback(lastOKSavePoint);
            throw new SQLDataInsertingException("inserting movie", e);
        }
    }

    protected int personExists(Person director) throws SQLException {
        PreparedStatement directorExists = connection.prepareStatement(findPersonQuery);
        DBModelMapper.setPersonData(director, directorExists);
        ResultSet result = directorExists.executeQuery();
        if (result.next()) {
            return result.getInt("id");
        } else{
            return 0;
        }
    }

    protected void insertPerson(Person person) throws SQLException {
        try {
            PreparedStatement directorInsert = connection.prepareStatement(insertPersonQuery, Statement.RETURN_GENERATED_KEYS);
            DBModelMapper.setPersonData(person, directorInsert);
            int a = directorInsert.executeUpdate();

            if (a == 0){
                throw new SQLException();
            } else {
                ResultSet generatedKeys = directorInsert.getGeneratedKeys();
                if (generatedKeys.next()) {
                    person.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Не удалось получить id созданного режиссера.");
                }
            }

//            connection.commit();
        } catch (SQLException e) {
            throw new SQLDataInsertingException("inserting person", e);
        }

    }

    protected void getOrInsertPerson(Person person) throws SQLException {
        int isDirector = personExists(person);
        if (isDirector == 0) {
            // Если режиссера нет, вставляем его в базу данных
            try {
                insertPerson(person);
            } catch (SQLDataInsertingException e) {
//                    connection.rollback(lastOKSavePoint);
                throw e;
            }
        }
        else {
            person.setId(isDirector);
        }
    }

    /**
     * метод проверяет существование пользователя с таким логином в базе данных
     * @param login
     * @return 0 - если пользователь не найден; id пользователя, если такой есть в базе
     * @throws SQLException
     */
    public int userExists(String login) throws SQLException {
        PreparedStatement userExists = connection.prepareStatement(findUserQuery);
        userExists.setString(1, login);

        ResultSet result = userExists.executeQuery();
        if (result.next()) {
            return result.getInt("id");
        } else{
            return 0;
        }
    }

    public String findUser(int id) throws SQLException {

        PreparedStatement userExists = connection.prepareStatement(findUserByIdQuery);
        userExists.setInt(1, id);

        ResultSet result = userExists.executeQuery();
        if (result.next()) {
            return result.getString("login");
        } else{
            return null;
        }
    }

    public void insertUser(User user, String salt) throws SQLException {
        int id = userExists(user.getLogin());

        if (id != 0){
            user.setId(id);
        } else{

            try {
                PreparedStatement userInsert = connection.prepareStatement(insertUserQuery, Statement.RETURN_GENERATED_KEYS);
                userInsert.setString(1, user.getLogin());
                userInsert.setString(2, user.getPassword());
                userInsert.setString(3, salt);

                int a = userInsert.executeUpdate();

                if (a == 0){
                    throw new SQLException();
                } else {
                    ResultSet generatedKeys = userInsert.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Не удалось получить id созданного пользователя.");
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                throw new SQLDataInsertingException("inserting user", e);
            }
        }

    }

    public String getUserSalt(String login) throws SQLException {
        PreparedStatement userSalt = connection.prepareStatement(getUserSaltQuery);
        userSalt.setString(1, login);

        ResultSet result = userSalt.executeQuery();
        if (result.next()) {
            return result.getString("salt");
        } else{
            return null;
        }
    }

    public String getUserPasswordHash(User user) throws SQLException {
        PreparedStatement userPwd = connection.prepareStatement(getUserPasswordHashQuery);
        userPwd.setString(1, user.getLogin());

        ResultSet result = userPwd.executeQuery();
        if (result.next()) {
            return result.getString("password_hash");
        } else{
            return null;
        }
    }
}
