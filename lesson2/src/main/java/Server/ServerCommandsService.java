package Server;

import Client.Client;
import Helpers.ChatCommandsHelper;
import Helpers.DatabaseHelper;

import java.util.ArrayList;

import static Helpers.ChatCommandsHelper.*;

public class ServerCommandsService {

    private Server server;

    public ServerCommandsService(Server server) {
        this.server = server;
    }

    //region Server commands
    synchronized void serverCommandListener(String command) {
        var parts = command.split("\\s");
        switch (parts[0]) {
            case HELP:
                server.sendLocalMessage(ChatCommandsHelper.getCommandHelp(parts.length == 1 ? parts[0] : parts[1]));
                break;
            case GET_COMMS:
                getServerCommandsCommand();
                break;
            case AUTH:
            case REG:
                server.sendLocalMessage("You don't need this commands");
                break;
            case LOGOUT:
                logoutServerCommand(parts);
                break;
            case PRIVATE_MSG:
                privateMsgServerCommand(parts);
                break;
            case GET_ONLINE:
                server.sendLocalMessage("Online users: \n" + server.getOnlineNicknames());
                break;
            case RENAME:
                renameServerCommand(parts);
                break;
            case END:
                server.stopServer();
                break;
            case DELETE:
                deleteServerCommand(parts);
                break;
            default:
                server.sendLocalMessage("Incorrect command");
        }
    }

    private void getServerCommandsCommand() {
        var out = new ArrayList<String>();
        out.addAll(ChatCommandsHelper.getAllClientCommands());
        out.addAll(ChatCommandsHelper.getAllServerCommands());
        server.sendLocalMessage(out.toString());
    }

    private void logoutServerCommand(String[] parts) {
        if (parts.length != 2) {
            server.sendLocalMessage("Incorrect command");
            return;
        }

        var client = server.getClientByNickname(parts[1]);
        if (client == null) {
            server.sendLocalMessage("Nickname not found");
            return;
        }
        client.sendMessage(parts[0]);
    }

    private void privateMsgServerCommand(String[] parts) {
        if (parts.length < 3) {
            server.sendLocalMessage("Incorrect command");
            return;
        }

        var recipient = server.findClientByNickname(parts[1]);
        if (recipient == null) {
            server.sendLocalMessage("Nickname not found");
            return;
        }
        server.sendPrivateServerMsg(recipient, connectWordsToMsg(parts, 2), true);
    }

    private void renameServerCommand(String[] parts) {
        if (parts.length != 3) {
            server.sendLocalMessage("Incorrect command");
            return;
        }
        var userNickname = parts[1];
        var userNewNickname = parts[2];
        if (DatabaseHelper.updateUserNickname(userNickname, userNewNickname, server.getPort())) {
            var client = server.getClientByNickname(userNickname);
            if (client.isOnline()) {
                client.changeTitle(userNewNickname);
            }
            server.broadcastMessage(String.format("%s now is %s!", userNickname, userNewNickname));
            return;
        }

        server.sendLocalMessage("Impossible change nickname.");
    }

    private void deleteServerCommand(String[] parts) {
        if (parts.length != 2) {
            server.sendLocalMessage("Incorrect command");
            return;
        }
        var userNickname = parts[1];
        var client = server.getClientByNickname(userNickname);
        if (DatabaseHelper.deleteUser(userNickname, server.getPort())) {
            server.sendLocalMessage(String.format("User \"%s\" is deleted.", userNickname));
            if (client != null) {
                client.sendMessage("/end");
            }
            return;
        }
        server.sendLocalMessage("User not found");
    }

    //endregion

    //region Client commands
    public synchronized void clientCommandListener(String command) {
        var parts = command.split("\\s");
        var sender = server.getClientByNickname(parts[0]);
        if (sender == null) {
            server.sendLocalMessage("Client command error: " + command);
            return;
        }
        switch (parts[1]) {
            case HELP:
                server.sendPrivateServerMsg(sender, ChatCommandsHelper.getCommandHelp(parts.length == 3 ? parts[2] : parts[1]), false);
                break;
            case GET_COMMS:
                server.sendPrivateServerMsg(sender, "Available commands: " + ChatCommandsHelper.getAllClientCommands().toString(), false);
                break;
            case AUTH:
                server.sendPrivateServerMsg(sender, "You already logged in server. If you want log in with other login, use \"" + LOGOUT + "\"", false);
                break;
            case REG:
                server.sendPrivateServerMsg(sender, "You no needed registration, you already logged in server.", false);
                break;
            case LOGOUT:
                server.clientLogOut(sender);
                break;
            case PRIVATE_MSG:
                privateMsgClientCommand(sender, parts);
                break;
            case GET_ONLINE:
                server.sendPrivateServerMsg(sender, "Online users: \n" + server.getOnlineNicknames(), false);
                break;
            case RENAME:
                renameClientCommand(sender, parts);
                break;
            default:
                server.sendPrivateServerMsg(sender, "Incorrect command", false);
        }
    }

    private synchronized void privateMsgClientCommand(Client sender, String[] parts) {
        var msg = connectWordsToMsg(parts, 3);
        var recipient = server.findClientByNickname(parts[2]);

        if (recipient == null) {
            server.sendPrivateServerMsg(sender, "Recipient not found. Please try again. Or use " + GET_ONLINE + " to get all online users.", false);
            return;
        }

        sender.sendMessage(String.format("Me -> %s: \"%s\"", parts[2], msg));
        recipient.sendMessage(String.format("%s -> Me: \"%s\"", parts[0], msg));
        server.sendLocalMessage(String.format("%s -> %s: \"%s\"", parts[0], parts[2], msg));
    }

    private void renameClientCommand(Client client, String[] parts) {
        if (parts.length != 3) {
            server.sendPrivateServerMsg(client, "Incorrect command.", false);
            return;
        }
        var userNickname = parts[0];
        var userNewNickname = parts[2];

        if (DatabaseHelper.updateUserNickname(userNickname, userNewNickname, server.getPort())) {
            client.changeTitle(userNewNickname);
            server.broadcastMessage(String.format("%s now is %s!", userNickname, userNewNickname));
            return;
        }
        server.sendPrivateServerMsg(client, "Impossible change nickname.", false);
    }
    //endregion


    private String connectWordsToMsg(String[] parts, int skipWordsCount) {
        var out = new StringBuilder();

        for (var i = 0; i < parts.length; i++) {
            if (i < skipWordsCount) continue;
            if (i == parts.length - 1) {
                out.append(parts[i]);
                break;
            }
            out.append(parts[i]).append(" ");
        }
        return out.toString();
    }
}

