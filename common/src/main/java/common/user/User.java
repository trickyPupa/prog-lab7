package common.user;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class User implements Serializable, Comparable<User> {
    private Integer id = null;
    private String login;
    private String password;

    public User() {}

    public User(int id, String login, String password) {
        this.login = login;
        this.password = password;
        this.id = id;
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int compareTo(User o) {
        return Objects.compare(this.login, o.login, Comparator.naturalOrder());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User)) return false;
        return Objects.equals(this.login, ((User) obj).login);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
