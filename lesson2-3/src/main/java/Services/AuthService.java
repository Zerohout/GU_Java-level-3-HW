package Services;

import Client.ClientHandler;
import Entities.Message;
import Entities.User;
import Helpers.DatabaseHelper;
import Server.ServerHandler;

import java.util.Timer;
import java.util.TimerTask;

import static Helpers.ChatCommandsHelper.*;
import static Services.MessageService.*;

public class AuthService {
    private ServerHandler server;
    private int port;
    private int clientIdleTime = 120;
    private int waitingClientStep = 20;
    public static final String AUTH_SERVICE_NAME = "Auth_Service_2.0";

    public AuthService(ServerHandler server) {
        this.server = server;
        this.port = server.getPort();
    }

    public synchronized void clientAuthentication(Message msg, ClientHandler client) {
        if (!msg.isCommand() || msg.getParts().length < 4) {
            sendMessage("You cannot send messages while not get authorization.", client);
            return;
        }
        var parts = splitText(msg.getText());

        switch (msg.getCommand()) {
            case AUTH -> {
                if (parts.length != 2) break;
                doAuthentication(parts, client);
                return;
            }
            case REG -> {
                if (parts.length != 3) break;
                doRegistration(parts, client);
                return;
            }
        }
        sendMessage("Incorrect data. Please try again", client);
    }

    private void doAuthentication(String[] parts, ClientHandler client) {
        var user = DatabaseHelper.getUser(parts[0], parts[1], this.port);
        if (user == null) {
            sendMessage("Incorrect login and/or password", client);
            return;
        }
        if (DatabaseHelper.isNicknameFree(user.getNickname(),server.getPort())) {
            user.isOnline(true);
            client.setUser(user);
            sendMessage(connectWords(AUTH_OK, user.getNickname()), client);
        } else {
            sendMessage(String.format("Nickname[%s] is already in use", user.getNickname()), client);
        }
    }

    private void doRegistration(String[] parts, ClientHandler client) {
        String text;
        if (DatabaseHelper.insertUser(new User(parts[0], parts[1], parts[2], this.port))) {
            text = String.format("Registration is done. Please get authorization by command \"%s login pass\"", AUTH);
        } else {
            text = String.format("Registration is not complete. Please try again or get authorization by command \"%s login pass\"", AUTH);
        }
        sendMessage(text, client);
    }

    private void sendMessage(String text, ClientHandler client) {
        var msg = createMessage(connectWords(AUTH_SERVICE_NAME, text),server);
        msg.addRecipient(client);
        msg.send();
    }

    public void setClientIdleWatcher(ClientHandler client) {
        var text = String.format("""
                Please get authorization by command "%s login password"
                or get registration by command "%s login password nickname\"""", AUTH, REG);
        sendMessage(text, client);
        var timer = new Timer(true);
        var timerAction = new TimerAction(client, timer);
        timer.schedule(timerAction, 0, waitingClientStep * 1000);
    }

    private class TimerAction extends TimerTask {
        private ClientHandler client;
        private int timerCount;
        private Timer timer;

        public TimerAction(ClientHandler client, Timer timer) {
            this.client = client;
            this.timer = timer;
        }

        @Override
        public void run() {
            if (client == null || !client.isOnline() || client.isAuth()) {
                timer.cancel();
            } else {
                if (timerCount >= clientIdleTime) {
                    sendMessage(END, client);
                    timer.cancel();
                } else {
                    var text = String.format("""
                    There are %d seconds left until the end of authentication.
                    After the time expires, you will be forcibly disconnected from the server.""",
                            clientIdleTime - timerCount);
                    sendMessage(text, client);
                }
            }
            timerCount += waitingClientStep;
        }
    }
}
