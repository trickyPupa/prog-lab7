package managers;

import common.model.entities.Movie;
import common.user.User;
import exceptions.WrongUserException;
import managers.data_base.PostgreDataBaseManager;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataBaseCollectionManager {
    private final CollectionManager collectionManager;
    private final PostgreDataBaseManager dataBaseManager;

    public DataBaseCollectionManager(CollectionManager cm, PostgreDataBaseManager db) {
        collectionManager = cm;
        dataBaseManager = db;
    }

    public DataBaseCollectionManager(PostgreDataBaseManager db) {
        dataBaseManager = db;
        collectionManager = new CollectionManager(db.getMovies());
    }

    public void add(Movie element, User user) throws SQLException {
        dataBaseManager.insertMovie(element, user.getId());
        collectionManager.add(element);
    }

    public void update(Movie element, User user) throws SQLException, WrongUserException {
        dataBaseManager.updateMovie(element, user.getId());
        sort();
    }

    public Movie remove(Movie i, User user) throws SQLException, WrongUserException {
        dataBaseManager.deleteMovie(i, user.getId());
        return collectionManager.remove(i);
    }

    public Movie remove(int i, User user) throws SQLException, WrongUserException {
        var mov = collectionManager.getCollection().get(i);
        dataBaseManager.deleteMovie(mov, user.getId());
//        return collectionManager.remove(i);
        return mov;
    }

    public Movie removeFirst(User user) throws SQLException {
        var mov = getCollectionForUser(user).stream().findFirst();

        if (mov.isPresent()) {
            try {
                return remove(mov.get(), user);
            } catch (WrongUserException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            return null;
        }
    }

    public void clear(User user) {
        collectionManager.getCollection().stream()
                .filter(x -> Objects.equals(x.getCreator(), user.getLogin()))
                .forEach(x -> {
                    try {
                        remove(x, user);
                    } catch (SQLException | WrongUserException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public String toString() {
        return collectionManager.toString();
    }

    public CopyOnWriteArrayList<Movie> getCollection() {
        return collectionManager.getCollection();
    }

    public CopyOnWriteArrayList<Movie> getCollectionForUser(User user) {
        CopyOnWriteArrayList<Movie> collection = new CopyOnWriteArrayList<>(collectionManager.getCollection()
                .stream()
                .filter((x) -> Objects.equals(x.getCreator(), user.getLogin())).toList());
        return collection;
    }

    public Map<String, String> getInfo() {
        return collectionManager.getInfo();
    }

    public boolean contains(Movie m) {
        return collectionManager.contains(m);
    }

    public String presentView() {
        return collectionManager.presentView();
    }

    public void sort() {
        collectionManager.sort();
    }
}
