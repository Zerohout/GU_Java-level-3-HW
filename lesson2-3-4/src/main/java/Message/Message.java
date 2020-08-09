package Message;

import Helpers.Sendable;

import java.io.Serializable;
import java.util.ArrayList;

import static Message.MessageBuilder.connectParts;

public class Message implements Serializable {
    private String text;
    private String sender;
    private String sendDate;
    private String command;
    private String[] commandArgs;
    private boolean isSystem;
    private boolean isPrivate;
    private boolean isCommand;
    private ArrayList<Sendable> recipients;

    public Message(String sendDate, String sender, String command, String[] commandArgs,
                   String text, ArrayList<Sendable> recipients,
                   boolean isSystem, boolean isPrivate, boolean isCommand) {
        this.sendDate = sendDate;
        this.sender = sender;
        this.command = command;
        this.commandArgs = commandArgs;
        this.text = text;
        this.recipients = recipients;
        this.isSystem = isSystem;
        this.isPrivate = isPrivate;
        this.isCommand = isCommand;
    }

    //region Getters
    public String getSendDate() {
        return sendDate;
    }

    public String getSender() {
        return this.sender;
    }

    public String getCommand() {
        return this.command;
    }

    public String[] getCommandArgs() {
        return this.commandArgs;
    }

    public String getText() {
        return this.text;
    }

    public ArrayList<Sendable> getRecipients() {
        return this.recipients;
    }

    public boolean isSystem() {
        return this.isSystem;
    }

    public boolean isPrivate() {
        return this.isPrivate;
    }

    public boolean isCommand() {
        return this.isCommand;
    }
    //endregion

    public void send() {
        for(var i = 0; i < recipients.size(); i++){
            if(recipients.get(i) == null) continue;
            if (isCommand && !isPrivate) {
                recipients.get(i).sendMessage(this);
                continue;
            }
            if(!isCommand && !isSystem && !isPrivate){
                recipients.get(i).sendMessage(this);
                continue;
            }
            recipients.get(i).sendLocalMessage(this);
        }
    }

    public void sendLocal(){
        for(var i = 0; i < recipients.size(); i++){
            recipients.get(i).sendLocalMessage(this);
        }
    }

    public String getConnectedText() {
        if (isCommand) {
            if (isPrivate) {
                return String.format("%s %s -> %s: %s",
                        this.sendDate, this.sender, recipients.get(1).getName(), text);
            }
            var args = connectParts(commandArgs, 0);
            return String.format("%s %s%s%s", this.sender, this.command,
                    args.equals("") ? args : " ", args);
        } else if (isSystem) {
            return String.format("%s: %s", this.sender, this.text);
        }
        return String.format("%s %s: %s", this.sendDate, this.sender, this.text);
    }
}
