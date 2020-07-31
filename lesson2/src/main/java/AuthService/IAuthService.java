package AuthService;

public interface IAuthService {
    void start();

    void stop();

    String getNickByLoginAndPass(String login, String password);

    boolean registerNewUser(String login, String pass, String nickname);
}
