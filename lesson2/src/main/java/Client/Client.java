package Client;

import AuthService.AuthService;
import Server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static Helpers.ChatCommandsHelper.*;

public class Client {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    private boolean isOnline = true;
    private boolean isAuth;

    public boolean isOnline() {
        return isOnline;
    }

    public Client(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = socket.toString();
            new Thread(this::readMessage).start();
        } catch (IOException e) {
            throw new RuntimeException("Error occurred during client initialization");
        }
    }

    public String getName() {
        return name;
    }

    public boolean isAuth() {
        return isAuth;
    }
    public void isAuth(boolean isAuth) {this.isAuth = isAuth;}

    private void readMessage() {
        while (isOnline) {
            String msg;
            try {
                msg = in.readUTF();
            } catch (IOException e) {
                //e.printStackTrace();
                close();
                return;
            }

            if (msg.startsWith("/")) {
                commandListener(msg);
            } else {
                if (!isAuth) {
                    server.getAuthService().clientAuthentication(msg, this);
                }
                server.broadcastMessage(String.format("%s: %s", name, msg));
            }
        }
    }

    public synchronized void sendMessage(String msg) {
        if (msg.startsWith(Server.SERVER_NAME) && msg.replace(Server.SERVER_NAME, "").startsWith("/")) {
            msg = msg.replace(Server.SERVER_NAME, "");
        }
        if (msg.startsWith("/")) {
            commandListener(msg);
            return;
        }
        try {
            if (out == null) close();
            else out.writeUTF(msg);
        } catch (IOException e) {
           // e.printStackTrace();
        }
    }

    private synchronized void commandListener(String msg) {
        if (msg.startsWith(END)) {
            close();
        } else if (msg.startsWith(AUTH_OK)) {
            authokCommand(msg);
        } else if (!isAuth) {
            server.getAuthService().clientAuthentication(msg,this);
        } else {
            msg = String.format("%s %s", name, msg);
            server.broadcastMessage(msg);
        }
    }

    private void authokCommand(String msg) {
        changeTitle(msg.split("\\s")[1]);
        server.subscribe(this);
    }

    public void changeTitle(String name){
        try {
            this.name = name;
            out.writeUTF(String.format("%s %s", SYS_TITLE, name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        isOnline = false;
        if (isAuth) server.unsubscribe(this);
        else server.sendLocalMessage(socket.toString() + " disconnected");
        try {
            if (!socket.isClosed()) out.writeUTF(END);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            in.close();
            out.close();
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
