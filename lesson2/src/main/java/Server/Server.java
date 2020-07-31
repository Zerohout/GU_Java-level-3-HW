package Server;

import Client.Client;
import Other.IAuthService;

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
                sendSystemMsgToClient(client,
                        "Please get authorization by command \"/auth login password\" or get registration by command \"/reg login password nickname\"");
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

    //region sending and getting messages
    public void sendServerMessage(String msg) {
        if (msg.startsWith("/")) {
            commandListener(msg, true);
        } else {
            msg = SERVER_NAME + msg;
            broadcastMessage(msg);
        }
    }

    public void sendSystemMsgToClient(Client client, String msg) {
        msg = SERVER_NAME + msg;
        client.sendMessage(msg);
    }

    public synchronized void broadcastMessage(String msg) {
        if (msg.startsWith("/")) {
            commandListener(msg, false);
            return;
        }
        serverApp.sendLocalMessage(msg);

        for (Client client : clients) {
            client.sendMessage(msg);
        }
    }

    public void sendLocalMessage(String msg) {
        serverApp.sendLocalMessage(msg);
    }
    //endregion

    private synchronized Client findClientByNickname(String nickname) {
        for (var client : clients) {
            if (client.getName().equals(nickname)) return client;
        }
        return null;
    }

    private void commandListener(String msg, boolean isServerMessage) {
        if (isServerMessage) serverCommandListener(msg);
        else clientCommandListener(msg);
    }

    private void clientCommandListener(String msg) {
        msg = msg.replace("/command ", "");

        var parts = msg.split("\\s");
        if (msg.contains("/t")) {
            var senderNick = parts[0];
            var recipientNick = parts[2];
            msg = connectWordsToMsg(parts);
            var sender = findClientByNickname(senderNick);
            var recipient = findClientByNickname(recipientNick);

            if (recipient == null) {
                sendSystemMsgToClient(sender, "User not found. Please try again. Or use /getall to get all available nicknames.");
                return;
            }
            if (sender == null) {
                sendLocalMessage(senderNick + " cannot send message");
                return;
            }

            sender.sendMessage(msg);
            recipient.sendMessage(msg);
            sendLocalMessage("to " + recipientNick + " -> " + msg);

        } else if (msg.contains("/getall")) {
            var nicknames = getAllNicknames();
            var sender = findClientByNickname(parts[0]);
            if (sender == null) {
                sendLocalMessage(parts[0] + " cannot send message");
                return;
            }
            sender.sendMessage("Nicknames: \n" + nicknames);
        }
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



    private synchronized void serverCommandListener(String msg) {
        if (msg.startsWith("/end")) {
            if(server.isClosed()) return;
            //notAuthClient.forEach(e -> e.sendMessage("/end"));
            var arr = new ArrayList<>(notAuthClients);
            //arr.addAll(clients);
            for(var i = 0; i < arr.size(); i++){
                arr.get(i).sendMessage("/end");
            }
            arr = new ArrayList<>(clients);
            for(var i = 0; i < arr.size(); i++){
                arr.get(i).sendMessage("/end");
            }

            stopServer();
        } else if (msg.startsWith("/t")) {
            var parts = msg.split("\\s");
            var nickname = parts[1];
            msg = msg.replace("/t ", "").replace(nickname + " ", "");

            var recipient = findClientByNickname(nickname);

            if (recipient == null) {
                sendLocalMessage("Nickname not found");
                return;
            }

            sendSystemMsgToClient(recipient, msg);

        } else if (msg.startsWith("/getall")) {
            var nicknames = new StringBuilder();
            for (var nick : getAllNicknames()) {
                nicknames.append(nick);
                nicknames.append("\n");
            }
            sendLocalMessage("Nicknames: \n" + nicknames);
        }
    }

    private ArrayList<String> getAllNicknames() {
        var out = new ArrayList<String>();
        for (var client : clients) {
            out.add(client.getName());
        }
        return out;
    }

    public synchronized void subscribe(Client client) {
        clients.add(client);
    }

    public synchronized void unsubscribe(Client client) {
        clients.remove(client);
    }

    private void stopServer() {
        if (server != null) {
            try {
                authService.stop();
                server.close();
                if(serverApp.isWorking()); serverApp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //region authentication
    public void clientAuthentication(Client client, String msg) {
        var parts = msg.split("\\s");

        if (msg.startsWith("/auth")) doAuth(client, parts);
        else if (msg.startsWith("/reg")) doReg(client, parts);
        else sendSystemMsgToClient(client, "Command is not found. Please use \"/auth\" or \"reg\"");
    }

    private void doAuth(Client client, String[] parts) {
        String nickname = authService.getNickByLoginAndPass(parts[1], parts[2]);
        if (nickname != null) {
            if (isNickFree(nickname)) {
                client.sendMessage("/authok " + nickname);
                sendServerMessage(nickname + " come in chat");
                notAuthClients.remove(client);
            } else {
                sendSystemMsgToClient(client, String.format("Nickname[%s] is already in use", nickname));
            }
        } else {
            sendSystemMsgToClient(client, "Incorrect login and/or password");
        }
    }

    private void doReg(Client client, String[] parts) {
        if (parts.length != 4) {
            sendSystemMsgToClient(client, "Incorrect data. Please try again");
            return;
        }

        var isRegSuccess = getAuthService().registerNewUser(parts[1], parts[2], parts[3]);
        if (isRegSuccess) {
            sendSystemMsgToClient(client, "Registration is done. Please get authorization by command /auth login pass");
        } else {
            sendSystemMsgToClient(client, "This user is already exist. Please get authorization by command /auth login pass");
        }
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isNickFree(String nickname) {
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
                    var msg = String.format("There are %d seconds left until the end of authentication. " +
                            "After the time expires, you will be forcibly disconnected from the server.", clientIdleTime - timerCount);
                    sendSystemMsgToClient(client, msg);
                }
            }
            timerCount += waitingClientStep;
        }
    }
    //endregion
}
