package AuthService;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String login;
    private String password;
    private String nickname;
    private boolean isOnline = true;
    private int serverPort;

    public User(String nickname, int serverPort) {
        this.nickname = nickname;
        this.serverPort = serverPort;
    }

    public User(String login, String password, String nickname, int serverPort) {
        this.login = login;
        this.password = password;
        this.nickname = nickname;
        this.serverPort = serverPort;
    }

    public String getLogin() {
        return this.login;
    }

    public String getPassword() {
        return this.password;
    }

    public String getNickname() {
        return this.nickname;
    }

    public int getServerPort() {
        return this.serverPort;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return this.id;
    }

    public boolean isOnline() {
        return this.isOnline;
    }

    public void isOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }
}
