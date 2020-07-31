package AuthService;

import Server.Server;

import java.util.ArrayList;
import java.util.List;


public class BaseAuthService implements IAuthService {
    private List<Entry> entries;
    private Server server;

    public BaseAuthService(Server server) {
        this.server = server;
        entries = new ArrayList<>();
    }

    @Override
    public void start() {
        server.sendLocalMessage("Server Auth 1.0 is starting...");
    }

    @Override
    public void stop() {
        server.sendLocalMessage("Server Auth 1.0 is showdown...");
    }

    @Override
    public String getNickByLoginAndPass(String login, String password) {
        for (Entry entry : entries) {
            if (entry.getLogin().equals(login) && entry.getPassword().equals(password)) {
                return entry.getNickname();
            }
        }
        return null;
    }

    public boolean registerNewUser(String login, String pass, String nickname) {
        var newUser = new Entry(login, pass, nickname);
        if (getNickByLoginAndPass(login, pass) != null) return false;

        entries.add(newUser);
        return true;
    }

    private class Entry {
        private String login;
        private String password;
        private String nickname;

        public Entry(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
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
    }
}
