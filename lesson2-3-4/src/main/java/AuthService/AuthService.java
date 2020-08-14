package AuthService;

import Client.ClientHandler;
import Message.Message;
import Message.MessageBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

import static Database.DatabaseHelper.*;
import static Helpers.ChatCommandsHelper.*;
import static Helpers.ControlPanel.getCurrentServer;
import static Message.MessageBuilder.connectWords;

public class AuthService {
    public static final String AUTH_SERVICE_NAME = "Auth_Service_2.0";
    private final Object monitor1 = new Object();
    private int clientIdleTime = 120;
    private int waitingClientStep = 20;
    private MessageBuilder mb;
    private static final Logger logger = LogManager.getLogger(AuthService.class);

    //region Reg auth methods
    public void clientAuthentication(Message msg, ClientHandler client) {
        logger.info("Поступило сообщение {} от клиента {}", msg.getConnectedText(), client.getName());
        if (!msg.isCommand()) {
            var text = "You cannot send messages while not get authorization.";
            sendMessageToClient(text, client);
            logger.warn("Обнаружена попытка отправки сообщения не авторизовавшись.");
            return;
        }
        synchronized (monitor1) {
            logger.info("Сообщение {} распознано как команда. Отправлено на обработку.", msg.getConnectedText());
            switch (msg.getCommand()) {
                case AUTH -> doAuthentication(msg.getCommandArgs(), client);
                case REG -> doRegistration(msg.getCommandArgs(), client);
                default -> {
                    var text = "Incorrect data. Please try again";
                    sendMessageToClient(text, client);
                    logger.warn("Команда {} не распознана.", msg.getCommand());
                }
            }
        }
    }

    private void doAuthentication(String[] args, ClientHandler client) {
        logger.info("Сообщение распознанно как команда /auth");
        if (args.length != 2) {
            var text = "Incorrect data. Please try again";
            sendMessageToClient(text, client);
            logger.warn("Команда /auth построена некорректно.");
            return;
        }
        var user = getUserByLoginPass(args[0], args[1]);
        if (user == null) {
            var text = "Incorrect login and/or password";
            sendMessageToClient(text, client);
            logger.warn("Клиент {} ввел некорректный логин и/или пароль.", client.getName());
            return;
        }

        if (user.isOnline()) {
            var text = String.format("Nickname[%s] is already in use", user.getNickname());
            sendMessageToClient(text, client);
            logger.warn("Попытка авторизоваться клиентом {} под ником {}. Клиент под данным ником уже онлайн.",
                    client.getName(), user.getNickname());
        } else {
            logger.info("Клиент {} авторизован и ему присвоен ник {}. Клиенту отправлена команда {}",
                    client.getName(), user.getNickname(), connectWords(AUTH_OK, user.getNickname()));
            user.isOnline(true);
            updateUserIsOnlineStatus(user.getNickname(), true);
            client.setUser(user);
            client.isAuth(true);
            mb.reset().compositeMessage(connectWords(AUTH_OK, user.getNickname())).setRecipients(client).build().send();
        }
    }

    private void doRegistration(String[] args, ClientHandler client) {
        logger.info("Сообщение распознанно как команда /reg");
        if (args.length != 3) {
            var text = "Incorrect data. Please try again";
            sendMessageToClient(text, client);
            logger.warn("Команда /reg построена некорректно.");
            return;
        }
        String text;
        if (addNewUserToDB(args)) {
            logger.info("В БД был добавлен новый пользователь. Логин: {}, пароль {}, никнейм {}.", args[0], args[1], args[2]);
            text = String.format("Registration is done. Please get authorization by command \"%s login pass\"", AUTH);
        } else {
            logger.warn("Новый пользователь не был добавлен в БД. Логин: {}, пароль {}, никнейм {}.", args[0], args[1], args[2]);
            text = String.format("Registration is not complete. Please try again or get authorization by command \"%s login pass\"", AUTH);
        }
        sendMessageToClient(text, client);
    }
    //endregion

    //region Technical methods
    public void start() {
        mb = new MessageBuilder();
        sendMessageToServer("started and ready to work.");
        logger.info("Сервис {} был включен.", AUTH_SERVICE_NAME);
    }

    private void sendMessageToClient(String text, ClientHandler client) {
        mb.reset().setAuthSystemMessage(text).setRecipients(client).build().send();
        logger.info("Клиенту {} отправлено сообщение: {}", client.getName(), text);
    }

    private void sendMessageToServer(String text) {
        mb.reset().setAuthSystemMessage(text).setRecipients(getCurrentServer()).build().send();
        logger.info("Серверу отправлено сообщение: {}", text);
    }

    public void stop() {
        sendMessageToServer("stopped.");
        logger.info("Сервис {} был выключен.", AUTH_SERVICE_NAME);
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
        return insertUser(newUser);
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
        logger.info("К клиенту {} был привязан наблюдатель. Таймер был включен", client.getName());
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
                logger.info("Таймер наблюдателя был остановлен. По одной из причин: клиент отсутствует, клиент оффлайн, клиент авторизован.");
                timer.cancel();
            } else {
                if (timerCount >= clientIdleTime) {
                    logger.info("Наблюдатель отключил клиента {} по причине истечения времени ожидания. Таймер был остановлен.", client.getName());
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
