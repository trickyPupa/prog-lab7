package network;

public class LoginCheckResponse extends Response {
    private boolean status;
    private String salt;

    public LoginCheckResponse(String msg, boolean status, String salt) {
        super(msg, null);
        this.status = status;
        this.salt = salt;
    }

    public boolean isLoginExists() {
        return status;
    }

    public String getSalt() {
        return salt;
    }
}
