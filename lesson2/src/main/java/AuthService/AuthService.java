package AuthService;

import Client.Client;
import Helpers.DatabaseHelper;
import Server.Server;

import java.util.Timer;
import java.util.TimerTask;

import static Helpers.ChatCommandsHelper.*;

public class AuthService {
    private Server server;
    private int port;
    private int clientIdleTime = 120;
    private int waitingClientStep = 20;
    private final String AUTH_SERVICE_NAME = "Auth_Service_2.0";

    public AuthService(Server server, int port) {
        this.server = server;
        this.port = port;
    }

    public synchronized void clientAuthentication(String msg, Client client) {
        if (!msg.startsWith("/")) {
            var sysMsg = "You cannot send messages while not get authorization.";
            client.sendMessage(String.format("%s: %s", AUTH_SERVICE_NAME, sysMsg));
            return;
        }
        var parts = msg.split("\\s");
        if (msg.startsWith(AUTH)) {
            doAuth(parts, client);
        } else if (msg.startsWith(REG)) {
            doRegistration(parts, client);
        } else {
            var sysMsg = String.format("%s: Command is not found. Please use %s of %s.",
                    AUTH_SERVICE_NAME, AUTH, REG);
            client.sendMessage(String.format("%s: %s", AUTH_SERVICE_NAME, sysMsg));
        }
    }

    private void doAuth(String[] parts, Client client) {
        if(parts.length != 3) {
            var sysMsg = "Incorrect data. Please try again";
            client.sendMessage(String.format("%s: %s", AUTH_SERVICE_NAME, sysMsg));
            return;
        }
        var user = DatabaseHelper.getUser(parts[1], parts[2], this.port);
        if (user == null) {
            var sysMsg = ("Incorrect login and/or password");
            client.sendMessage(String.format("%s: %s", AUTH_SERVICE_NAME, sysMsg));
        } else {
            if (isNickFree(user.getNickname())) {
                var command = AUTH_OK + " " + user.getNickname();
                client.sendMessage(command);
            } else {
                var sysMsg = (String.format("Nickname[%s] is already in use", user.getNickname()));
                client.sendMessage(String.format("%s: %s", AUTH_SERVICE_NAME, sysMsg));
            }
        }
    }

    private void doRegistration(String[] parts, Client client) {
        if (parts.length != 4) {
            var sysMsg = "Incorrect data. Please try again";
            client.sendMessage(String.format("%s: %s", AUTH_SERVICE_NAME, sysMsg));
            return;
        }
        if (DatabaseHelper.insertUser(new User(parts[1], parts[2], parts[3], this.port))) {
            var sysMsg = String.format("Registration is done. Please get authorization by command \"%s login pass\"", AUTH);
            client.sendMessage(String.format("%s: %s", AUTH_SERVICE_NAME, sysMsg));
        } else {
            var sysMsg = String.format("Registration is not complete. Please try again or get authorization by command \"%s login pass\"", AUTH);
            client.sendMessage(String.format("%s: %s", AUTH_SERVICE_NAME, sysMsg));
        }
    }

    private synchronized boolean isNickFree(String nickname) {
        for (Client client : server.getOnlineClients()) {
            if (client.getName().equals(nickname)) {
                return false;
            }
        }
        return true;
    }

    public void setClientIdleWatcher(Client client) {
        var sysMsg = String.format("""
                Please get authorization by command "%s login password"
                or get registration by command "%s login password nickname\"""", AUTH, REG);
        client.sendMessage(String.format("%s: %s", AUTH_SERVICE_NAME, sysMsg));
        var timer = new Timer(true);
        var timerAction = new TimerAction(client, timer);
        timer.schedule(timerAction, 0, waitingClientStep * 1000);
    }

    private class TimerAction extends TimerTask {
        private Client client;
        private int timerCount;
        private Timer timer;

        public TimerAction(Client client, Timer timer) {
            this.client = client;
            this.timer = timer;
        }

        @Override
        public void run() {
            if (client == null || client.isAuth()) {
                timer.cancel();
            } else {
                if (timerCount >= clientIdleTime) {
                    client.sendMessage(END);
                    timer.cancel();
                } else {
                    var msg = String.format("""
                    There are %d seconds left until the end of authentication.
                    After the time expires, you will be forcibly disconnected from the server.""",
                            clientIdleTime - timerCount);

                    client.sendMessage(String.format("%s: %s", AUTH_SERVICE_NAME, msg));
                }
            }
            timerCount += waitingClientStep;
        }
    }
}
