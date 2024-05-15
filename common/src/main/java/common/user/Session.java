package common.user;

public class Session {
    private User user;
    private String salt;

    public Session(User user, String salt) {
        this.user = user;
        this.salt = salt;
    }

    public User getUser() {
        return user;
    }
    public String getSalt() {
        return salt;
    }
}
