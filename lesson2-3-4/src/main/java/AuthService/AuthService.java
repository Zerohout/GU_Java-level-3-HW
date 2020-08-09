package AuthService;

import Client.ClientHandler;
import Message.Message;
import Message.MessageBuilder;

import java.util.Timer;
import java.util.TimerTask;

import static Helpers.ChatCommandsHelper.*;
import static Helpers.ControlPanel.getCurrentServer;
import static Database.DatabaseHelper.*;
import static Message.MessageBuilder.*;

public class AuthService {
    public static final String AUTH_SERVICE_NAME = "Auth_Service_2.0";
    private final Object monitor1 = new Object();
    private int clientIdleTime = 120;
    private int waitingClientStep = 20;
    private MessageBuilder mb;

    //region Reg auth methods
    public void clientAuthentication(Message msg, ClientHandler client) {
        if (!msg.isCommand()) {
            sendMessageToClient("You cannot send messages while not get authorization.", client);
            return;
        }
        synchronized (monitor1) {
            switch (msg.getCommand()) {
                case AUTH -> doAuthentication(msg.getCommandArgs(), client);
                case REG -> doRegistration(msg.getCommandArgs(), client);
                default -> sendMessageToClient("Incorrect data. Please try again", client);
            }
        }
    }

    private void doAuthentication(String[] args, ClientHandler client) {
        if (args.length != 2) {
            sendMessageToClient("Incorrect data. Please try again", client);
            return;
        }
        var user = getUserByLoginPass(args[0], args[1]);
        if (user == null) {
            sendMessageToClient("Incorrect login and/or password", client);
            return;
        }

        if (user.isOnline()) {
            sendMessageToClient(String.format("Nickname[%s] is already in use", user.getNickname()), client);
        } else {
            user.isOnline(true);
            updateUserIsOnlineStatus(user.getNickname(), true);
            client.setUser(user);
            client.isAuth(true);
            mb.reset().compositeMessage(connectWords(AUTH_OK, user.getNickname())).setRecipients(client).build().send();
        }
    }

    private void doRegistration(String[] args, ClientHandler client) {
        if (args.length != 3) {
            sendMessageToClient("Incorrect data. Please try again", client);
            return;
        }
        String text;
        if (addNewUserToDB(args)) {
            text = String.format("Registration is done. Please get authorization by command \"%s login pass\"", AUTH);
        } else {
            text = String.format("Registration is not complete. Please try again or get authorization by command \"%s login pass\"", AUTH);
        }
        sendMessageToClient(text, client);
    }
    //endregion

    //region Technical methods
    public void start() {
        mb = new MessageBuilder();
        sendMessageToServer("started and ready to work.");
    }

    private void sendMessageToClient(String text, ClientHandler client) {
        mb.reset().setAuthSystemMessage(text).setRecipients(client).build().send();
    }

    private void sendMessageToServer(String text) {
        mb.reset().setAuthSystemMessage(text).setRecipients(getCurrentServer()).build().send();
    }

    public void stop() {
        sendMessageToServer("stopped.");
    }
    //endregion

    //region Users operation methods
    private User getUserByNickname(String nickname) {
        var users = getAllUsers();
        if (users == null) return null;
        for (var user : users) {
            if (user.isNicknameCorrect(nickname)) return user;
        }
        return null;
    }

    private User getUserByLogin(String login) {
        var users = getAllUsers();
        if (users == null) return null;
        for (var user : users) {
            if (user.isLoginCorrect(login)) return user;
        }
        return null;
    }

    private User getUserByLoginPass(String login, String password) {
        var users = getAllUsers();
        if (users == null) return null;
        for (var user : users) {
            if (user.isLoginPassCorrect(login, password)) return user;
        }
        return null;
    }

    private boolean addNewUserToDB(String[] args) {
        var newUser = new UserBuilder().setLogin(args[0]).setPassword(args[1]).setNickname(args[2]).isOnline(true).build();
        if (insertUser(newUser)) {
            return true;
        }
        return false;
    }
    //endregion

    //region ClientIdleWatcher
    public void setClientIdleWatcher(ClientHandler client) {
        var text = String.format("""
                Please get authorization by command "%s login password"
                or get registration by command "%s login password nickname\"""", AUTH, REG);
        sendMessageToClient(text, client);
        var timer = new Timer(true);
        var timerAction = new TimerAction(client, timer);
        timer.schedule(timerAction, 0, waitingClientStep * 1000);
    }

    private class TimerAction extends TimerTask {
        private ClientHandler client;
        private int timerCount;
        private Timer timer;

        TimerAction(ClientHandler client, Timer timer) {
            this.client = client;
            this.timer = timer;
        }

        @Override
        public void run() {
            if (client == null || !client.isOnline() || client.isAuth()) {
                timer.cancel();
            } else {
                if (timerCount >= clientIdleTime) {
                    mb.reset().compositeMessage(END).setRecipients(client).build().send();
                    timer.cancel();
                } else {
                    var text = String.format("""
                    There are %d seconds left until the end of authentication.
                    After the time expires, you will be forcibly disconnected from the server.""",
                            clientIdleTime - timerCount);
                    sendMessageToClient(text, client);
                }
            }
            timerCount += waitingClientStep;
        }
    }
    //endregion
}
