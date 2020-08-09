package Message;

import Helpers.Sendable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static AuthService.AuthService.AUTH_SERVICE_NAME;
import static Server.ServerHandler.SERVER_NAME;

public class MessageBuilder {
    private String sendDate = new SimpleDateFormat("dd.MM.yy_HH:mm:ss").format(new Date());
    private String sender = "";
    private String command = "";
    private String[] commandArgs = new String[0];
    private String text = "";
    private ArrayList<Sendable> recipients = new ArrayList<>();
    private boolean isSystem;
    private boolean isPrivate;
    private boolean isCommand;

    //region Setters
    public MessageBuilder setSendDate(String sendDate){
        this.sendDate = sendDate;
        return this;
    }

    public MessageBuilder setSender(String sender) {
        this.sender = sender;
        return this;
    }

    public MessageBuilder setCommand(String command) {
        this.isCommand = true;
        this.command = command;
        return this;
    }

    public MessageBuilder setCommandArgs(String[] commandArgs) {
        this.commandArgs = commandArgs;
        return this;
    }

    public MessageBuilder setText(String text) {
        this.text = text;
        return this;
    }

    public MessageBuilder setRecipients(ArrayList<Sendable> recipients) {
        this.recipients = recipients;
        return this;
    }

    public MessageBuilder setRecipients(Sendable... recipients) {
        this.recipients = new ArrayList<>(Arrays.asList(recipients));
        return this;
    }

    public MessageBuilder addRecipients(Sendable... recipients) {
        this.recipients.addAll(Arrays.asList(recipients));
        return this;
    }

    public MessageBuilder isSystem(boolean isSystem) {
        this.isSystem = isSystem;
        return this;
    }

    public MessageBuilder isPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        return this;
    }

    public MessageBuilder isCommand(boolean isCommand) {
        this.isCommand = isCommand;
        return this;
    }

    public MessageBuilder reset(){
        return new MessageBuilder();
    }
    //endregion

    //region builds
    public MessageBuilder setServerSystemMessage(String text) {
        return setSender(SERVER_NAME).setText(text).isSystem(true).isCommand(false);
    }

    public MessageBuilder setAuthSystemMessage(String text) {
        return setSender(AUTH_SERVICE_NAME).setText(text).isSystem(true).isCommand(false);
    }

    public MessageBuilder compositeMessage(String text) {
        if (text.startsWith("/")) text = connectWords(SERVER_NAME, text);
        var parts = text.split("\\s");
        this.sender = parts[0];
        if (parts[1].startsWith("/")) {
            return setCommand(parts[1]).setCommandArgs(cutParts(parts, 2)).setText(text);
        }
        return setText(connectParts(parts, 1));
    }

    public MessageBuilder convertMsgToBuilder(Message msg){
        return reset().setSendDate(msg.getSendDate()).setSender(msg.getSender()).setCommand(msg.getCommand())
                .setCommandArgs(msg.getCommandArgs()).setText(msg.getText()).setRecipients(msg.getRecipients())
                .isSystem(msg.isSystem()).isPrivate(msg.isPrivate()).isCommand(msg.isCommand());
    }
    //endregion

    //region Technical methods
    public static String[] cutParts(String[] parts, int startIndex) {
        var size = parts.length;
        if (startIndex >= size) return new String[0];
        var out = new String[size - startIndex];
        for (int i = 0, j = startIndex; i < out.length; i++, j++) {
            out[i] = parts[j];
        }
        return out;
    }

    public static String connectParts(String[] parts, int startIndex) {
        var size = parts.length;
        if (size == 0) return "";
        var out = new StringBuilder();
        for (var i = startIndex; i < size; i++) {
            out.append(parts[i]);
            if (i < size - 1) out.append(" ");
        }
        return out.toString();
    }

    public static String connectWords(String... words) {
        var out = new StringBuilder();
        var size = words.length;
        for (var i = 0; i < size; i++) {
            out.append(words[i]);
            if (i != size - 1) {
                out.append(" ");
            }
        }
        return out.toString();
    }
    //endregion

    public Message build() {
        return new Message(sendDate, sender, command, commandArgs, text, recipients, isSystem, isPrivate, isCommand);
    }
}
