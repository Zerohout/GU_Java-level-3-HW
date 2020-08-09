package Client;

import AuthService.UserBuilder;
import AuthService.User;
import AuthService.AuthService;
import Database.DatabaseHelper;
import Helpers.Sendable;
import Server.ServerHandler;
import Message.MessageBuilder;
import Message.Message;

import static Helpers.ChatCommandsHelper.*;
import static Message.MessagesWriterReader.*;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ClientHandler implements Serializable, Sendable {
    transient private ServerHandler server;
    transient private Socket socket;
    transient private AuthService authService;
    transient private ClientCommandHandler commandHandler;
    transient private DataOutputStream out;
    transient private DataInputStream in;
    private boolean isAuth = false;
    private User user;
    private MessageBuilder mb;
    private ArrayList<String> messages;

    public ClientHandler(ServerHandler server, Socket socket) {
        messages = new ArrayList<>();
        mb = new MessageBuilder();
        this.server = server;
        this.socket = socket;
        try {
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.user = new UserBuilder().setNickname(socket.toString()).build();
        this.authService = server.getAuthService();
        this.commandHandler = new ClientCommandHandler(this, server);
        new Thread(this::readMessage).start();
    }

    private void readMessage() {
        try {
            while (!socket.isClosed()) {
                mb.reset().compositeMessage(in.readUTF()).addRecipients(this).build().send();
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void sendMessage(Message msg) {
        if (msg.isSystem() || msg.isPrivate()) sendLocalMessage(msg);
        else if (!isAuth && !msg.getCommand().equals(END)) doAuthentication(msg);
        else if (msg.isCommand()) commandHandler.commandListener(msg);
        else server.broadcastMessage(msg,false);
    }

    public void sendLocalMessage(Message msg) {
        try {
            var text = msg.getConnectedText();
            if (text.startsWith(new SimpleDateFormat("dd.MM.yy").format(new Date()))) {
                if (messages.size() == 100) {
                    addMessagesToFile(getLogin(), messages);
                    messages.clear();
                }
                messages.add(msg.getConnectedText());
            }
            out.writeUTF(msg.getConnectedText());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void recoveryMessages(ArrayList<String> messages) {
        try {
            for (var text : messages) {
                out.writeUTF(text);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void doAuthentication(Message msg) {
        authService.clientAuthentication(msg, this);
    }

    public void updateUser() {
        this.user = DatabaseHelper.getUser(user.getId());
        DatabaseHelper.closeConnection();
    }

    //region Getters and Setters
    public void setUser(User user) {
        this.user = user;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public void isAuth(boolean isAuth) {
        if (isAuth) {
            recoveryMessages(getMessagesFromFile(getLogin()));
        } else {
            addMessagesToFile(getLogin(), messages);
            messages.clear();
        }
        this.isAuth = isAuth;
    }

    public boolean isOnline() {
        return user.isOnline();
    }

    public boolean isNicknameCorrect(String nickname) {
        return user.isNicknameCorrect(nickname);
    }

    @Override
    public String getName() {
        return this.user.getNickname();
    }

    public String getLogin() {
        return this.user.getLogin();
    }
    //endregion

    public void closeClientHandler() throws IOException {
        sendLocalMessage(mb.reset().compositeMessage(END).build());
        in.close();
        out.close();
        socket.close();
        DatabaseHelper.updateUserIsOnlineStatus(getName(),false);
    }


}
