package Services;

import Client.ClientHandler;
import Entities.Message;
import Server.ServerHandler;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static Helpers.ChatCommandsHelper.*;

public class MessageService {
    public enum CommandType {
        SERVICE,
        TOTAL,
        SYSTEM,
        WRONG
    }

    public static void addMessagesToFile(ArrayList<Message> messages, int serverPort) {
        if (messages == null) return;
        try (var oos = new ObjectOutputStream(new FileOutputStream("./messages/messages_" + serverPort + ".msg"))) {
            oos.writeObject(messages);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static ArrayList<Message> getMessagesFromFile(int serverPort) {
        try (var oos = new ObjectInputStream(new FileInputStream("./messages/messages_" + serverPort + ".msg"))) {
            return (ArrayList<Message>) oos.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            //ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static boolean isNotValid(String text) {
        return text == null || text.isEmpty() || text.isBlank();
    }

    public static Message createMessage(String text, ServerHandler server) {
        if (isNotValid(text)) return new Message("", "", server);
        var parts = splitText(text);
        if (parts.length >= 2) {
            var isCommand = parts[1].startsWith("/");
            if (isCommand) {
                return new Message(parts[0], parts[1], substringText(parts, 2), getCommandType(parts[1]), server);
            }
        }

        if (parts[0].equals(parts[1])) {
            return new Message(parts[1], substringText(parts, 2), server);
        }

        return new Message(parts[0], substringText(parts, 1), server);
    }

    public static Message createPrivateMessage(String text, ClientHandler sender, ClientHandler recipient, ServerHandler server) {
        if (isNotValid(text)) return new Message("", "", server);
        var msg = new Message(sender == null ? "SERVER" : sender.getNickname(), PRIVATE_MSG, text, getCommandType(PRIVATE_MSG), server);
        if (sender == null) {
            msg.addRecipient(recipient);
        } else {
            msg.addRecipients(sender, recipient);
        }
        msg.setSendDate(getCurrentDate());
        return msg;
    }

    public static Message createLocalMessage(String text, ServerHandler server) {
        if (isNotValid(text)) return new Message("", "", server);
        var parts = splitText(text);
        if (parts.length >= 2) {
            var isCommand = parts[1].startsWith("/");
            if (isCommand) {
                return new Message(parts[0], parts[1], substringText(parts, 2), getCommandType(parts[1]), server);
            }
        }
        return new Message(parts[0], substringText(parts, 1), getCurrentDate(), server);
    }

    private static String getCurrentDate() {
        var dateFormat = new SimpleDateFormat("dd/MM/yy_HH:mm:ss");
        return dateFormat.format(new Date());
    }

    public static String substringText(String[] parts, int startIndex) {
        var endIndex = parts.length;
        if (startIndex >= endIndex) return "";
        StringBuilder out = new StringBuilder();

        for (var i = startIndex; i < endIndex; i++) {
            out.append(parts[i]);
            if (i < endIndex - 1) {
                out.append(" ");
            }
        }
        return out.toString();
    }

    private static CommandType getCommandType(String command) {
        return switch (command) {
            case AUTH, REG, AUTH_OK -> CommandType.SERVICE;
            case PRIVATE_MSG, GET_COMMS, GET_ONLINE, HELP -> CommandType.TOTAL;
            case LOGOUT, DELETE, RENAME, END -> CommandType.SYSTEM;
            default -> CommandType.WRONG;
        };
    }

    public static String[] splitText(String text) {
        return text.split("\\s");
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
}
