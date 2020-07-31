package Server;

import AuthService.BaseAuthService;
import Client.Client;
import AuthService.IAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private final int port;
    private IAuthService authService;
    private Set<Client> clients;
    private Set<Client> notAuthClients;
    private ServerApp serverApp;
    private ServerSocket server;
    public final static String SERVER_NAME = "SERVER: ";

    private int clientIdleTime = 120;
    private int waitingClientStep = 20;

    public Server(int port, ServerApp serverApp) {
        this.port = port;
        this.clients = new HashSet<>();
        this.notAuthClients = new HashSet<>();
        this.serverApp = serverApp;
        new Thread(this::start).start();
    }

    private void start() {
        try (ServerSocket server = new ServerSocket(this.port)) {
            authService = new BaseAuthService(this);
            authService.start();
            this.server = server;

            while (true) {
                serverApp.sendLocalMessage("Server started on port: " + port);
                serverApp.sendLocalMessage("Server is waiting for clients...");
                Socket socket = server.accept();
                serverApp.sendLocalMessage(String.format("Client connected: %s", socket.toString()));
                var client = new Client(this, socket);
                sendSystemMsgToClient(client, """
                Please get authorization by command \"/auth login password\"
                or get registration by command \"/reg login password nickname\"""", false);
                clientIdleWatcher(client);
                notAuthClients.add(client);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    //region sending messages
    public synchronized void broadcastMessage(String msg) {
        if (msg.startsWith("/")) {
            clientCommandListener(msg);
            return;
        }
        serverApp.sendLocalMessage(msg);

        for (Client client : clients) {
            client.sendMessage(msg);
        }
    }

    public void sendServerMessage(String msg) {
        if (msg.startsWith("/")) {
            serverCommandListener(msg);
        } else {
            msg = SERVER_NAME + msg;
            broadcastMessage(msg);
        }
    }

    public synchronized void sendSystemMsgToClient(Client client, String msg, boolean isMsgWatcherEnabled) {
        msg = SERVER_NAME + msg;
        client.sendMessage(msg);
        if (!isMsgWatcherEnabled) return;
        sendLocalMessage("to " + client.getName() + " -> " + msg);
    }

    public void sendLocalMessage(String msg) {
        serverApp.sendLocalMessage(msg);
    }
    //endregion

    //region Chat commands

    //region Server commands
    private synchronized void serverCommandListener(String msg) {
        if (msg.startsWith("/end")) {
            endServerCommand();
        } else if (msg.startsWith("/t")) {
            privateMsgServerCommand(msg);
        } else if (msg.startsWith("/getall")) {
            sendLocalMessage("Nicknames: \n" + getAllNicknames());
        } else {
            sendLocalMessage("Incorrect command");
        }
    }


    private void endServerCommand() {
        if (server.isClosed()) return;
        var arr = new ArrayList<>(notAuthClients);
        for (var client : arr) {
            client.sendMessage("/end");
        }
        arr = new ArrayList<>(clients);
        for (var client : arr) {
            client.sendMessage("/end");
        }
        stopServer();
    }

    private void privateMsgServerCommand(String msg) {
        var parts = msg.split("\\s");
        var recipient = findClientByNickname(parts[1]);
        if (recipient == null) {
            sendLocalMessage("Nickname not found");
            return;
        }
        msg = msg.substring(3).replace(recipient.getName(), "").substring(1);
        sendSystemMsgToClient(recipient, msg, true);
    }
    //endregion

    //region Client commands
    private void clientCommandListener(String msg) {
        if (msg.startsWith("/command")) msg = msg.replace("/command ", "");
        var parts = msg.split("\\s");
        var sender = findClientByNickname(parts[0]);
        if (sender == null) {
            sendLocalMessage("Sender is not found. Message: \"" + msg + "\"");
            return;
        }

        if (msg.contains("/t")) {
            privateMsgClientCommand(sender, parts);
        } else if (msg.contains("/getall")) {
            sender.sendMessage("Nicknames: \n" + getAllNicknames());
        } else {
            sender.sendMessage("Incorrect command. Try again");
            sendLocalMessage(msg);
        }
    }

    private void privateMsgClientCommand(Client sender, String[] parts) {
        var msg = connectWordsToMsg(parts);
        var recipient = findClientByNickname(parts[2]);

        if (recipient == null) {
            sendSystemMsgToClient(sender, "Recipient not found. Please try again. Or use /getall to get all available nicknames.", false);
            return;
        }

        sender.sendMessage(msg);
        recipient.sendMessage(msg);
        sendLocalMessage("to " + recipient.getName() + " -> " + msg);
    }
    //endregion

    //endregion

    //region Technical methods
    public synchronized void subscribe(Client client) {
        clients.add(client);
        sendServerMessage(client.getName() + " come in chat");
    }

    public synchronized void unsubscribe(Client client) {
        clients.remove(client);
        sendServerMessage(client.getName() + " left the chat");
    }

    private ArrayList<String> getAllNicknames() {
        var out = new ArrayList<String>();
        for (var client : clients) {
            out.add(client.getName());
        }
        return out;
    }

    private String connectWordsToMsg(String[] parts) {
        var out = new StringBuilder();

        for (var i = 0; i < parts.length; i++) {
            if (i > 0 && i < 3) continue;
            if (i == parts.length - 1) {
                out.append(parts[i]);
                break;
            }
            if (i == 0) out.append(parts[i]).append(": ");
            else out.append(parts[i]).append(" ");
        }
        return out.toString();
    }

    private synchronized Client findClientByNickname(String nickname) {
        for (var client : clients) {
            if (client.getName().equals(nickname)) return client;
        }
        return null;
    }

    private void stopServer() {
        if (server != null) {
            try {
                authService.stop();
                server.close();
                if (serverApp.isWorking()) serverApp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //endregion

    //region authentication
    public void clientAuthentication(Client client, String msg) {
        var parts = msg.split("\\s");

        if (msg.startsWith("/auth")) {
            doAuth(client, parts);
        } else if (msg.startsWith("/reg")) {
            doRegistration(client, parts);
        } else {
            sendSystemMsgToClient(client, "Command is not found. Please use \"/auth\" or \"reg\"", false);
        }
    }

    private void doAuth(Client client, String[] parts) {
        String nickname = authService.getNickByLoginAndPass(parts[1], parts[2]);
        if (nickname != null) {
            if (isNickFree(nickname)) {
                client.sendMessage("/authok " + nickname);

                notAuthClients.remove(client);
            } else {
                sendSystemMsgToClient(client, String.format("Nickname[%s] is already in use", nickname), false);
            }
        } else {
            sendSystemMsgToClient(client, "Incorrect login and/or password", false);
        }
    }

    private void doRegistration(Client client, String[] parts) {
        if (parts.length != 4) {
            sendSystemMsgToClient(client, "Incorrect data. Please try again", false);
            return;
        }

        var isRegSuccess = getAuthService().registerNewUser(parts[1], parts[2], parts[3]);
        if (isRegSuccess) {
            sendSystemMsgToClient(client, "Registration is done. Please get authorization by command /auth login pass", false);
        } else {
            sendSystemMsgToClient(client, "This user is already exist. Please get authorization by command /auth login pass", false);
        }
    }

    private IAuthService getAuthService() {
        return authService;
    }

    private synchronized boolean isNickFree(String nickname) {
        return !isNickBusy(nickname);
    }

    private synchronized boolean isNickBusy(String nickname) {
        for (Client client : clients) {
            if (client.getName().equals(nickname)) {
                return true;
            }
        }
        return false;
    }
    //endregion

    //region client watcher
    private void clientIdleWatcher(Client client) {
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
            if (client.isAuth()) {
                timer.cancel();
            } else {
                if (timerCount >= clientIdleTime) {
                    client.sendMessage("/end");
                    timer.cancel();
                } else {
                    var msg = String.format("""
                    There are %d seconds left until the end of authentication.
                    After the time expires, you will be forcibly disconnected from the server.""",
                            clientIdleTime - timerCount);

                    sendSystemMsgToClient(client, msg, false);
                }
            }
            timerCount += waitingClientStep;
        }
    }
    //endregion
}
