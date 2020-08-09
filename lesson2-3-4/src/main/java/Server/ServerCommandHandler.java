package Server;

import Database.DatabaseHelper;
import Message.Message;
import Message.MessageBuilder;

import static Helpers.ChatCommandsHelper.*;
import static Message.MessageBuilder.*;
import static Server.ServerHandler.SERVER_NAME;
import static Server.ServerHandler.getClientByNickname;

public class ServerCommandHandler {
    private ServerHandler server;
    private MessageBuilder mb;

    public ServerCommandHandler(ServerHandler server) {
        mb = new MessageBuilder();
        this.server = server;
    }

    synchronized void serverCommandListener(Message msg) {
        switch (msg.getCommand()) {
            case HELP -> executeHelpCommand(msg);
            case GET_COMMS -> executeGetCommsCommand();
            case AUTH, REG, LOGOUT, RENAME -> executeUnneededCommands();
            case PRIVATE_MSG -> executePrivateMsgCommand(msg);
            case GET_ONLINE -> executeGetOnline();
            case END -> executeEndCommand();
            case DELETE -> executeDeleteCommand(msg);
            default -> sendServerSystemMessage("Incorrect command");
        }
    }

    // SERVER /help
    // SERVER /help (0)/command
    private void executeHelpCommand(Message msg) {
        var args = msg.getCommandArgs();
        sendServerSystemMessage(getCommandHelp(args.length == 0 ? msg.getCommand() : args[0]));
    }

    // SERVER /getcomms
    private void executeGetCommsCommand() {
        var commands = getAllClientCommands();
        commands.addAll(getAllServerCommands());
        sendServerSystemMessage(commands.toString());
    }

    private void executeUnneededCommands() {
        sendServerSystemMessage("You don't need this commands");
    }

    // SERVER /t (0)recipient (1...)text
    // SERVER /t (0)recipient (1)/command (2...)args
    // SERVER /t (0)recipient (1)/t (2)/recipient (3...)text
    private void executePrivateMsgCommand(Message msg) {
        var args = msg.getCommandArgs();
        if (args.length < 2) {
            sendServerSystemMessage("Incorrect command");
            return;
        }
        var recipient = getClientByNickname(args[0]);
        if (recipient == null) {
            sendServerSystemMessage("Nickname not found");
            return;
        }
        if (args[1].startsWith("/")) {
            mb.reset().compositeMessage(connectParts(args, 0)).setRecipients(recipient).build().send();
        } else {
            mb.reset().isCommand(true).isPrivate(true).setSender(SERVER_NAME)
                    .setRecipients(server, recipient).setText(connectParts(args, 1)).build().send();
        }
    }

    // SERVER /delete (0)nickname
    private void executeDeleteCommand(Message msg) {
        var args = msg.getCommandArgs();
        if(args.length == 0) {
            sendServerSystemMessage("Incorrect command");
            return;
        }
        var nickname = args[0];

        var client = getClientByNickname(nickname);
        if (DatabaseHelper.deleteUser(nickname)) {
            sendServerSystemMessage(String.format("User \"%s\" is deleted.", nickname));
            if (client != null) {
                mb.reset().compositeMessage(END).setRecipients(client).build().send();
            }
            return;
        }
        sendServerSystemMessage("User not found");
    }

    private void executeGetOnline() {
        sendServerSystemMessage("Online users: \n" + server.getNicknames());
    }

    private void executeEndCommand() {
        server.stopServer();
    }

    private void sendServerSystemMessage(String text) {
        mb.reset().setServerSystemMessage(text).setRecipients(server).build().send();
    }
}

