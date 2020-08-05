package Entities;

import Client.ClientHandler;
import Server.ServerHandler;
import Services.AuthService;

import static Services.MessageService.*;
import static Helpers.ChatCommandsHelper.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Message implements Serializable {
    private String text = "";
    private String sender = "";
    private String sendDate = "";
    private String command = "";
    private boolean isLocal;
    private boolean isPrivate;
    private boolean isRecovery;
    private boolean isCommand = false;
    transient private ServerHandler server;
    private CommandType commandType;
    private ArrayList<ClientHandler> recipients = new ArrayList<>();

    public Message(String sender, String command, String text, CommandType commandType, ServerHandler server) {
        this.sender = sender;
        this.text = text;
        this.command = command;
        this.commandType = commandType;
        this.isCommand = true;
        this.isPrivate = command.equals(PRIVATE_MSG);
        this.isLocal = false;
        this.server = server;
    }

    public Message(String sender, String text, String sendDate, ServerHandler server) {
        this.sender = sender;
        this.text = text;
        this.sendDate = sendDate;
        this.isLocal = true;
        this.server = server;
    }

    public Message(String sender, String text, ServerHandler server) {
        this.sender = sender;
        this.text = text;
        this.isLocal = false;
        this.server = server;
    }

    public void addRecipients(List<ClientHandler> recipients) {
        this.recipients.addAll(recipients);
    }

    public void addRecipients(ClientHandler... recipients) {
        this.recipients.addAll(Arrays.asList(recipients));
    }

    public void addRecipient(ClientHandler recipient) {
        this.recipients.add(recipient);
    }

    //region Getters and setters
    public String getText() {
        return this.text;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getSender() {
        return this.sender;
    }

    public ArrayList<ClientHandler> getRecipients() {
        return this.recipients;
    }

    public boolean isCommand() {
        return this.isCommand;
    }

    public String getCommand() {
        return this.command;
    }

    public String[] getParts() {
        return splitText(connectWords(this.sender, this.command, this.text));
    }

    public boolean isPrivate() {
        return this.isPrivate;
    }

    public void isRecovery(boolean isRecovery){this.isRecovery = isRecovery;}
    //endregion

    public void send() {
        recoverySave();
        for (var client : getRecipients()) {
            if (client == null) continue;
            if (isPrivate) {
                client.sendLocalMessage(this);
            } else {
                if (isCommand) {
                    client.sendMessage(this);
                } else {
                    client.sendLocalMessage(this);
                }
            }
        }
    }

    private void recoverySave(){
        if (!isRecovery && !sender.equals(AuthService.AUTH_SERVICE_NAME) && !sender.equals(ServerHandler.SERVER_NAME)) {
            if (!isCommand || isPrivate) {
                server.addRecoveryMessage(this);
                this.isRecovery = false;
            }
        }
    }

    public String getConnectedText() {
        if (isCommand) {
            if (isPrivate) {
                if (recipients.size() >= 1) {
                    return String.format("%s %s -> %s: %s", this.sendDate, this.sender,
                            this.recipients.get(recipients.size() - 1).getNickname(), this.text);
                }
            }
            return connectWords(this.sender, this.command, this.text);
        }
        if (isLocal) return connectWords(this.sendDate, this.sender, this.text);
        return connectWords(this.sender, this.text);
    }
}
