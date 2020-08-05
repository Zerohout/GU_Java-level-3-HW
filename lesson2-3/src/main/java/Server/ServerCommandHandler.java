package Server;

import Helpers.ChatCommandsHelper;
import Helpers.DatabaseHelper;
import Entities.Message;

import java.util.ArrayList;

import static Helpers.ChatCommandsHelper.*;
import static Services.MessageService.*;

public class ServerCommandHandler {

    private ServerHandler server;

    public ServerCommandHandler(ServerHandler server) {
        this.server = server;
    }

    synchronized void serverCommandListener(Message msg) {
        var command = msg.getCommand();
        if (command == null) return;
        var parts = msg.getParts();

        switch (command) {
            case HELP -> server.sendLocalMessage(getCommandHelp(parts.length == 2 ? msg.getCommand() : msg.getText()));
            case GET_COMMS -> executeGetCommsCommand();
            case AUTH, REG -> server.sendLocalMessage("You don't need this commands");
            case LOGOUT -> executeLogoutCommand(msg);
            case PRIVATE_MSG -> executePrivateMsgCommand(msg);
            case GET_ONLINE -> server.sendLocalMessage("Online users: \n" + ChatCommandsHelper.getNicknames(new ArrayList<>(server.getOnlineClients())));
            case RENAME -> executeRenameCommand(splitText(msg.getText()));
            case END -> server.stopServer();
            case DELETE -> executeDeleteCommand(msg);
            default -> server.sendLocalMessage("Incorrect command");
        }
    }

    private void executeGetCommsCommand() {
        var out = new ArrayList<String>();
        out.addAll(getAllClientCommands());
        out.addAll(getAllServerCommands());
        server.sendLocalMessage(out.toString());
    }

    private void executeLogoutCommand(Message msg) {
        if (splitText(msg.getText()).length != 1) {
            server.sendLocalMessage("Incorrect command");
            return;
        }
        var client = server.getClientByNickname(splitText(msg.getText())[0]);
        if (client == null) {
            server.sendLocalMessage("Nickname not found");
            return;
        }
        msg.addRecipient(client);
        msg.send();
    }

    private void executePrivateMsgCommand(Message msg) {
        if (msg.getParts().length < 4) {
            server.sendLocalMessage("Incorrect command");
            return;
        }
        var recipient = server.getClientByNickname(splitText(msg.getText())[0]);
        if (recipient == null) {
            server.sendLocalMessage("Nickname not found");
            return;
        }
        server.sendPrivateServerMsg(recipient, substringText(msg.getParts(), 3), true);
    }

    private void executeRenameCommand(String[] parts) {
        if (parts.length != 2) {
            server.sendLocalMessage("Incorrect command");
            return;
        }
        var userNickname = parts[0];
        var userNewNickname = parts[1];

        if (DatabaseHelper.isNicknameFree(userNewNickname, server.getPort()) &&
                DatabaseHelper.updateUserNickname(userNickname, userNewNickname, server.getPort())) {
            var client = server.getClientByNickname(userNickname);
            if (client != null) {
                var msg = createMessage(connectWords(ServerHandler.SERVER_NAME, RENAME, userNewNickname),server);
                msg.addRecipient(client);
                msg.send();
            }
            server.broadcastMessage(createMessage(connectWords(ServerHandler.SERVER_NAME, String.format("%s now is %s!", userNickname, userNewNickname)),server));
            return;
        }
        server.sendLocalMessage("Impossible change nickname.");
    }


    private void executeDeleteCommand(Message msg) {
        if (msg.getParts().length != 3) {
            server.sendLocalMessage("Incorrect command");
            return;
        }
        var userNickname = msg.getText();
        var client = server.getClientByNickname(userNickname);
        if (DatabaseHelper.deleteUser(userNickname, server.getPort())) {
            server.sendLocalMessage(String.format("User \"%s\" is deleted.", userNickname));
            if (client != null) {
                client.sendMessage(createMessage(connectWords(ServerHandler.SERVER_NAME, END),server));
            }
            return;
        }
        server.sendLocalMessage("User not found");
    }
}

