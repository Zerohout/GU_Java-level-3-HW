package Client;

import Entities.User;
import Server.ServerHandler;
import Services.AuthService;
import Helpers.DatabaseHelper;
import Entities.Message;
import Services.MessageService;

import static Helpers.ChatCommandsHelper.*;
import static Services.MessageService.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;


public class ClientHandler implements Serializable {
    transient private ServerHandler server;
    transient private Socket socket;
    transient private AuthService authService;
    transient private ClientCommandHandler commandHandler;
    transient private DataOutputStream out;
    transient private DataInputStream in;
    private boolean isAuth = false;
    private Message msg;
    private User user;

    public ClientHandler(ServerHandler server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.user = new User(socket.toString(), server.getPort());
        this.authService = server.getAuthService();
        this.commandHandler = new ClientCommandHandler(this, server);
        new Thread(this::readMessage).start();
    }

    private void readMessage() {
        try {
            while (!socket.isClosed()) {
                msg = createLocalMessage(in.readUTF(),server);
                if (msg.isCommand()) {
                    commandHandler.commandListener(msg);
                    continue;
                }
                sendMessage(msg);
            }
        } catch (IOException ex) {
            return;
        }
    }

    public void sendMessage(Message msg) {
        if (!isAuth) {
            if (!msg.getSender().equals(AuthService.AUTH_SERVICE_NAME)) {
                if (msg.isCommand() && msg.getSender().equals(ServerHandler.SERVER_NAME)) {
                    commandHandler.commandListener(msg);
                    return;
                }
                this.authService.clientAuthentication(msg, this);
            } else {
                if (msg.isCommand()) {
                    commandHandler.commandListener(msg);
                }
                sendLocalMessage(msg);
            }
            return;
        }
        if (msg.isCommand()) {
            commandHandler.commandListener(msg);
            return;
        }
        server.broadcastMessage(msg);
    }

    public void sendLocalMessage(Message msg) {
        try {
            out.writeUTF(msg.getConnectedText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doAuthentication() {
        authService.clientAuthentication(msg, this);
    }

    public void updateUser() {
        this.user = DatabaseHelper.getUser(user.getId());
    }

    //region Getters and Setters
    public void setUser(User user) {
        this.user = user;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public void isAuth(boolean isAuth) {
        this.isAuth = isAuth;
    }

    public boolean isOnline() {
        return user.isOnline();
    }

    public String getNickname() {
        return this.user.getNickname();
    }
    //endregion

    public void closeClientHandler() throws IOException {
        sendLocalMessage(createMessage(connectWords(getNickname(), END),server));
        in.close();
        out.close();
        socket.close();
        user.isOnline(false);
    }
}
