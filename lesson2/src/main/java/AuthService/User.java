package AuthService;

public class User {
    private int id;
    private String login;
    private String password;
    private String nickname;
    private int serverPort;

    public User(String login, String password, String nickname, int serverPort) {
        this.login = login;
        this.password = password;
        this.nickname = nickname;
        this.serverPort = serverPort;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setId(int id) {
        this.id = id;
    }
}
