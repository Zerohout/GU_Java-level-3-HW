package Server;

import AuthService.AuthService;
import Client.Client;
import Helpers.ChatCommandsHelper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static Helpers.ChatCommandsHelper.*;

public class Server {
    private final int port;
    private HashSet<Client> onlineClients;
    private HashSet<Client> notAuthClients;
    private ServerApp serverApp;
    private ServerSocket server;
    private AuthService authService;
    private ServerCommandsService commandsService;
    public final static String SERVER_NAME = "SERVER: ";

    public Server(int port, ServerApp serverApp) {
        this.port = port;
        this.onlineClients = new HashSet<>();
        this.notAuthClients = new HashSet<>();
        this.serverApp = serverApp;
        new Thread(this::start).start();
    }

    private void start() {
        try (ServerSocket server = new ServerSocket(this.port)) {
            authService = new AuthService(this, this.port);
            commandsService = new ServerCommandsService(this);
            this.server = server;
            serverApp.sendLocalMessage("Server started on port: " + port);
            while (true) {
                serverApp.sendLocalMessage("Server is waiting for clients...");
                Socket socket = server.accept();
                serverApp.sendLocalMessage(String.format("Client connected: %s", socket.toString()));
                var client = new Client(this, socket);
                notAuthClients.add(client);
                authService.setClientIdleWatcher(client);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    int getPort() {
        return port;
    }

    public AuthService getAuthService() {
        return this.authService;
    }

    public HashSet<Client> getOnlineClients() {
        return onlineClients;
    }

    Client getClientByNickname(String nickname) {
        for (var client : onlineClients) {
            if (client.getName().equals(nickname)) return client;
        }
        return null;
    }

    //region sending messages
    public synchronized void broadcastMessage(String msg) {
        var parts = msg.split("\\s");
        if (parts.length > 1 && parts[1].startsWith("/")) {
            commandsService.clientCommandListener(msg);
            return;
        }
        serverApp.sendLocalMessage(msg);

        for (Client client : onlineClients) {
            client.sendMessage(msg);
        }
    }

    void sendServerMessage(String msg) {
        if (msg.startsWith("/")) {
            commandsService.serverCommandListener(msg);
        } else {
            msg = SERVER_NAME + msg;
            broadcastMessage(msg);
        }
    }

    synchronized void sendPrivateServerMsg(Client client, String msg, boolean isMsgWatcherEnabled) {
        client.sendMessage(SERVER_NAME + msg);
        if (!isMsgWatcherEnabled) return;
        sendLocalMessage(String.format("SERVER -> %s: \"%s\"", client.getName(), msg));
    }

    public void sendLocalMessage(String msg) {
        serverApp.sendLocalMessage(msg);
    }
    //endregion

    //region Technical methods
    public synchronized void subscribe(Client client) {
        notAuthClients.remove(client);
        onlineClients.add(client);
        client.isAuth(true);
        sendServerMessage(client.getName() + " come in chat");
    }

    public synchronized void unsubscribe(Client client) {
        onlineClients.remove(client);
        client.isAuth(false);
        sendServerMessage(client.getName() + " left the chat");
    }

    synchronized void clientLogOut(Client client) {
        unsubscribe(client);
        notAuthClients.add(client);
    }

    ArrayList<String> getOnlineNicknames() {
        var out = new ArrayList<String>();
        for (var client : onlineClients) {
            out.add(client.getName());
        }
        return out;
    }

    synchronized Client findClientByNickname(String nickname) {
        for (var client : onlineClients) {
            if (client.getName().equals(nickname)) return client;
        }
        return null;
    }

    void stopServer() {
        if (server.isClosed()) return;
        var arr = new ArrayList<>(notAuthClients);
        for (var client : arr) {
            client.sendMessage(END);
        }
        arr = new ArrayList<>(onlineClients);
        for (var client : arr) {
            client.sendMessage(END);
        }

        if (server != null) {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serverApp.isWorking()) serverApp.close();
    }
    //endregion
}
