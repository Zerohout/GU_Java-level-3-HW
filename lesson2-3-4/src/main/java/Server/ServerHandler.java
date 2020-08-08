package Server;

import Client.ClientHandler;
import AuthService.AuthService;
import Message.Message;
import Message.MessageService;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static Helpers.ChatCommandsHelper.*;
import static Message.MessageService.*;

public class ServerHandler {
    private final int port;
    private ServerSocket serverSocket;
    private ServerApp serverApp;
    private AuthService authService;
    private HashSet<ClientHandler> onlineClients;
    private HashSet<ClientHandler> notAuthClients;
    private ServerCommandHandler commandHandler;
    public final static String SERVER_NAME = "SERVER";
    private boolean isStopped;
    private ArrayList<Message> messages;

    //region Getters
    public int getPort() {
        return port;
    }

    public AuthService getAuthService() {
        return this.authService;
    }

    public HashSet<ClientHandler> getOnlineClients() {
        return onlineClients;
    }

    public ClientHandler getClientByNickname(String nickname) {
        for (var client : onlineClients) {
            if (client.getNickname().equals(nickname)) return client;
        }
        return null;
    }

    //endregion

    public ServerHandler(int port, ServerApp serverApp) {
        this.port = port;
        this.onlineClients = new HashSet<>();
        this.notAuthClients = new HashSet<>();
        this.serverApp = serverApp;
        new Thread(this::start).start();
    }

    private void start() {
        try (ServerSocket server = new ServerSocket(this.port)) {
            this.serverSocket = server;
            authService = new AuthService(this);
            commandHandler = new ServerCommandHandler(this);
            serverApp.sendLocalMessage("Server started on port: " + port);
            createServerMessagesFile();
            this.messages = new ArrayList<>(MessageService.getMessagesFromFile(port));
            showLastMessages();
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
        MessageService.addMessagesToFile(messages, port);
        isStopped = true;
        closeClients();
        serverApp.close();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //region Messages
    private void createServerMessagesFile() {
        try {
            new File("./messages/messages_" + port + ".msg").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addRecoveryMessage(Message msg){
        this.messages.add(msg);
    }

    private void showLastMessages(){
        if(messages == null || messages.size() == 0) return;
        var size = messages.size();
        var startIndex = size <= 100 ? 0 : size - 100;
        for (var i = startIndex; i < size; i++){
            sendLocalMessage(messages.get(i).getConnectedText());
        }
    }

    private void loadClientMessages(ClientHandler client) {
        if (this.messages == null) return;
        var size = messages.size();
        if (size == 0) return;
        var clientMessages = new ArrayList<Message>();
        for (int i = 0; i < size; i++) {
            var msg = messages.get(i);
            if (isContains(msg, client)) {
                clientMessages.add(msg);
            }
        }
        size = clientMessages.size();
        var startIndex = size <= 100 ? 0 : size - 100;
        for(var i = startIndex; i < size; i++){
            client.sendLocalMessage(clientMessages.get(i));
        }
    }

    private static boolean isContains(Message msg, ClientHandler client) {
        for (var i = 0; i < msg.getRecipients().size(); i++) {
            if (msg.getRecipients().get(i).getNickname().equals(client.getNickname())) {
                return true;
            }
        }
        return false;
    }
    //endregion

    //region sending messages
    public synchronized void broadcastMessage(Message msg) {
        if (msg.isCommand()) {
            return;
        }
        msg.addRecipients(new ArrayList<>(onlineClients));
        msg.send();
        sendLocalMessage(msg.getConnectedText());
    }

    public void sendMessage(Message msg) {
        if (msg.isCommand()) {
            commandHandler.serverCommandListener(msg);
            return;
        }
        broadcastMessage(msg);
    }

    synchronized void sendPrivateServerMsg(ClientHandler recipient, String text, boolean isMsgWatcherEnabled) {
        var msg = MessageService.createPrivateMessage(text, null, recipient,this);
        msg.send();
        if (!isMsgWatcherEnabled) return;
        sendLocalMessage(msg.getConnectedText());
    }

    public void sendLocalMessage(String text) {
        serverApp.sendLocalMessage(text);
    }
    //endregion

    //region Clients methods
    public synchronized void subscribe(ClientHandler client) {
        notAuthClients.remove(client);
        onlineClients.add(client);
        loadClientMessages(client);
        client.isAuth(true);
        broadcastMessage(createMessage(connectWords(SERVER_NAME, client.getNickname(), "come in chat"),this));
    }

    public synchronized void unsubscribe(ClientHandler client) {
        onlineClients.remove(client);
        client.isAuth(false);
        addNotAuthClient(client);
        broadcastMessage(createMessage(connectWords(SERVER_NAME, client.getNickname(), "left the chat"),this));
    }

    private void addNotAuthClient(ClientHandler client) {
        notAuthClients.add(client);
        authService.setClientIdleWatcher(client);
    }

    private void closeClients() {
        var msg = createMessage(connectWords(SERVER_NAME, END),this);
        msg.addRecipients(new ArrayList<>(notAuthClients));
        msg.addRecipients(new ArrayList<>(onlineClients));
        msg.send();
    }

    public void closeClient(ClientHandler client) throws IOException {
        onlineClients.remove(client);
        notAuthClients.remove(client);
        if (client.isAuth()) {
            broadcastMessage(createMessage(connectWords(SERVER_NAME, client.getNickname(), "left the chat"),this));
        } else {
            sendLocalMessage(connectWords(SERVER_NAME, client.getNickname(), "disconnected"));
        }
        client.closeClientHandler();
    }
    //endregion
}

