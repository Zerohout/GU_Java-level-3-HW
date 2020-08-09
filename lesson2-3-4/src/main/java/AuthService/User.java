package AuthService;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String login;
    private String password;
    private String nickname;
    private boolean isOnline;

    public User(String login, String password, String nickname, boolean isOnline) {
        this.login = login;
        this.password = password;
        this.nickname = nickname;
        this.isOnline = isOnline;
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

    public boolean isLoginPassCorrect(String login, String password){
        return this.login.equals(login) && this.password.equals(password);
    }

    public boolean isLoginCorrect(String login){
        return this.login.equals(login);
    }

    public boolean isNicknameCorrect(String nickname){
        return this.nickname.equals(nickname);
    }
}
