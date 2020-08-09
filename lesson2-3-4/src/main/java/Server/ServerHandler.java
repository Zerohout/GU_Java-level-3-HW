package Server;

import Client.ClientHandler;
import AuthService.AuthService;
import Database.DatabaseHelper;
import Helpers.Sendable;
import Message.Message;
import Message.MessageBuilder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static Database.DatabaseHelper.*;
import static Helpers.ChatCommandsHelper.*;
import static Message.MessageBuilder.*;

public class ServerHandler implements Sendable {
    private final int port;
    private ServerSocket serverSocket;
    private ServerApp serverApp;
    private AuthService authService;
    private HashSet<ClientHandler> notAuthClients;
    private static HashSet<ClientHandler> onlineClients = new HashSet<>();
    private ServerCommandHandler commandHandler;
    public final static String SERVER_NAME = "SERVER";
    private boolean isStopped;
    private MessageBuilder mb;

    //region Getters
    public AuthService getAuthService() {
        return this.authService;
    }

    @Override
    public String getName() {
        return SERVER_NAME;
    }

//    public HashSet<ClientHandler> getOnlineClients() {
//        return onlineClients;
//    }

    public static ClientHandler getClientByNickname(String nickname) {
        for (var client : onlineClients) {
            if (client.isNicknameCorrect(nickname)) return client;
        }
        return null;
    }

    public ArrayList<String> getNicknames() {
        var out = new ArrayList<String>();
        for (var client : onlineClients) {
            out.add(client.getName());
        }
        return out;
    }

    //endregion

    public ServerHandler(int port, ServerApp serverApp) {
        this.mb = new MessageBuilder();
        this.port = port;
        this.notAuthClients = new HashSet<>();
        this.serverApp = serverApp;
        new Thread(this::start).start();
    }

    private void start() {
        try (ServerSocket server = new ServerSocket(this.port)) {
            this.serverSocket = server;
            authService = new AuthService();
            authService.start();
            commandHandler = new ServerCommandHandler(this);
            serverApp.sendLocalMessage("Server started on port: " + port);
            while (!server.isClosed() && !isStopped) {
                Socket socket = server.accept();
                serverApp.sendLocalMessage(String.format("Client connected on %s", socket.toString()));
                var client = new ClientHandler(this, socket);
                addNotAuthClient(client);
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        } finally {
            if (!isStopped) {
                closeClients();
                if (serverApp != null) serverApp.close();
            }
        }
    }

    public void stopServer() {
        authService.stop();
        isStopped = true;
        closeClients();
        serverApp.close();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //region sending messages
    public synchronized void broadcastMessage(Message msg, boolean isSystem) {
        mb.convertMsgToBuilder(msg).isSystem(isSystem)
                .setRecipients(new ArrayList<>(onlineClients))
                .addRecipients(this).build().sendLocal();
    }

    @Override
    public void sendMessage(Message msg) {
        if (msg.isCommand()) {
            commandHandler.serverCommandListener(msg);
            return;
        }
        broadcastMessage(msg, false);
    }

    @Override
    public void sendLocalMessage(Message msg) {
        serverApp.sendLocalMessage(msg.getConnectedText());
    }
    //endregion

    //region Clients methods
    public synchronized void subscribe(ClientHandler client) {
        notAuthClients.remove(client);
        onlineClients.add(client);
        broadcastMessage(mb.reset().setRecipients(this).setServerSystemMessage(connectWords(client.getName(), "come in chat.")).build(), true);
    }

    public synchronized void unsubscribe(ClientHandler client) {
        DatabaseHelper.updateUserIsOnlineStatus(client.getName(), false);
        onlineClients.remove(client);
        client.isAuth(false);
        addNotAuthClient(client);
        broadcastMessage(mb.reset().setRecipients(this).setServerSystemMessage(connectWords(client.getName(), "left the chat.")).build(), true);
    }

    private void addNotAuthClient(ClientHandler client) {
        notAuthClients.add(client);
        authService.setClientIdleWatcher(client);
    }

    private void closeClients() {
        var users = DatabaseHelper.getOnlineUsers();
        if (users == null) throw new RuntimeException("users is null");
        for (var i = 0; i < users.size(); i++) {
            var client = getClientByNickname(users.get(i).getNickname());
            if (client != null) {
                unsubscribe(client);
            }
        }
        setUsersStatusToOffline();
        mb = mb.reset().compositeMessage(END);
        mb.setRecipients(new ArrayList<>(notAuthClients)).build().send();
        mb.setRecipients(new ArrayList<>(onlineClients)).build().send();
    }

    public void closeClient(ClientHandler client) throws IOException {
        DatabaseHelper.updateUserIsOnlineStatus(client.getName(), false);
        onlineClients.remove(client);
        notAuthClients.remove(client);
        if (client.isAuth()) {
            unsubscribe(client);
        } else {
            mb.reset().setServerSystemMessage(connectWords(client.getName(), "disconnected"))
                    .setRecipients(this).build().send();
        }
        client.closeClientHandler();
    }
    //endregion
}

