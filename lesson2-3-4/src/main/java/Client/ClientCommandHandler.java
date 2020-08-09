package Client;

import Database.DatabaseHelper;
import Server.ServerHandler;
import Message.Message;
import Message.MessageBuilder;

import java.io.File;
import java.io.IOException;

import static Helpers.ChatCommandsHelper.*;
import static Message.MessageBuilder.*;

public class ClientCommandHandler {
    private ClientHandler client;
    private ServerHandler server;
    private MessageBuilder mb;

    public ClientCommandHandler(ClientHandler client, ServerHandler server) {
        this.client = client;
        this.server = server;
        mb = new MessageBuilder();
    }

    public synchronized void commandListener(Message msg) {
        var command = msg.getCommand();
        if (command == null) return;

        switch (command) {
            case HELP -> executeHelpCommand(msg);
            case GET_COMMS -> executeGetCommsCommand();
            case AUTH, REG -> executeUnneededCommand();
            case AUTH_OK -> executeAuthOkCommand(msg);
            case LOGOUT -> executeLogoutCommand();
            case RENAME -> executeRenameCommand(msg);
            case PRIVATE_MSG -> executePrivateMsgCommand(msg);
            case END -> executeEndCommand();
            case GET_ONLINE -> executeGetOnlineCommand();
            default -> sendIncorrectCommandMessage();
        }
    }

    // sender /help
    // sender /help (0)arg
    private void executeHelpCommand(Message msg) {
        var args = msg.getCommandArgs();
        sendServerSystemMessage(getCommandHelp(args.length == 0 ? msg.getCommand() : args[0]));
    }


    private void executeGetCommsCommand() {
        sendServerSystemMessage(getAllClientCommands().toString());
    }


    private void executeUnneededCommand() {
        sendServerSystemMessage("You don't need this commands");
    }

    // sender /authok (0)nickname
    private void executeAuthOkCommand(Message msg) {
        mb.convertMsgToBuilder(msg).isSystem(true).setRecipients(client)
                .setText(msg.getText()).build().send();
        server.subscribe(client);
    }



    private void executeLogoutCommand() {
        server.unsubscribe(client);
    }

    // sender /t (0)recipient (1...)text
    private void executePrivateMsgCommand(Message msg) {
        var recipient = ServerHandler.getClientByNickname(msg.getCommandArgs()[0]);
        if (recipient == null) {
            sendServerSystemMessage("User not found");
            return;
        }
        mb.reset().setSender(client.getName()).isCommand(true).isPrivate(true).addRecipients(client, recipient)
                .setText(connectParts(msg.getCommandArgs(), 1)).build().send();
    }

    private void executeEndCommand() {
        try {
            server.closeClient(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeGetOnlineCommand() {
        sendServerSystemMessage("Online users: \n" + server.getNicknames());
    }

    private void sendIncorrectCommandMessage() {
        sendServerSystemMessage("Incorrect command");
    }

    // sender /rename (0)login (1)pass (2)new_nickname
    private void executeRenameCommand(Message msg) {
        var args = msg.getCommandArgs();
        if(args.length < 3) sendIncorrectCommandMessage();
        var user = DatabaseHelper.getUser(args[0], args[1]);
        if (user == null) {
            sendServerSystemMessage("Incorrect login/password.");
            return;
        }

        if (DatabaseHelper.isNicknameFree(args[2]) &&
                DatabaseHelper.updateUserNickname(args[0],args[1],args[2])) {
            client.updateUser();
            client.sendLocalMessage(mb.convertMsgToBuilder(msg).setCommandArgs(new String[]{args[2]}).build());
        } else {
            sendServerSystemMessage("Impossible change nickname");
        }
    }


    private void sendServerSystemMessage(String text) {
        mb.reset().setServerSystemMessage(text).setRecipients(client).build().send();
    }
}
