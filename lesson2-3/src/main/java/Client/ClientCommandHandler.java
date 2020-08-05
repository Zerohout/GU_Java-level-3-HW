package Client;

import Helpers.ChatCommandsHelper;
import Helpers.DatabaseHelper;
import Server.ServerHandler;
import Entities.Message;

import java.io.IOException;
import java.util.ArrayList;

import static Helpers.ChatCommandsHelper.*;
import static Services.MessageService.*;
import static Server.ServerHandler.*;

public class ClientCommandHandler {

    private ClientHandler client;
    private ServerHandler server;

    public ClientCommandHandler(ClientHandler client, ServerHandler server) {
        this.client = client;
        this.server = server;
    }

    private void sendMessage(Message msg) {
        client.sendLocalMessage(msg);
    }

    public synchronized void commandListener(Message msg) {
        var command = msg.getCommand();
        if (command == null) return;

        switch (command) {
            case HELP -> executeHelpCommand(msg);
            case GET_COMMS -> executeGetCommsCommand();
            case AUTH, REG -> executeRegCommand();
            case AUTH_OK -> server.subscribe(client);
            case LOGOUT -> server.unsubscribe(client);
            case PRIVATE_MSG -> executePrivateMsgCommand(msg);
            case RENAME -> executeRenameCommand(msg);
            case END -> executeEndCommand();
            case GET_ONLINE -> executeGetOnlineCommand();
            default -> executeDefaultCommand();
        }
    }

    private void executeHelpCommand(Message msg) {
        sendMessage(createMessage(connectWords(SERVER_NAME, getCommandHelp(msg.getParts().length == 2 ? msg.getCommand() : msg.getText())), server));
    }

    private void executeGetCommsCommand(){
        sendMessage(createMessage(connectWords(SERVER_NAME, "Available commands:", getAllClientCommands().toString()), server));
    }

    private void executeRegCommand() {
        if (!client.isAuth()) {
            client.doAuthentication();
            return;
        }
        sendMessage(createMessage(connectWords(SERVER_NAME, "You already logged in server. If you want log in with other login, use \"" + LOGOUT + "\""), server));
    }

    private void executePrivateMsgCommand(Message msg){
        if (msg.getRecipients().size() == 0) {
            msg = createPrivateMessage(substringText(splitText(msg.getText()), 1), client,
                    server.getClientByNickname(splitText(msg.getText())[0]), server);
        }
        msg.send();
    }

    private void executeRenameCommand(Message msg){
        if (DatabaseHelper.isNicknameFree(msg.getText(), server.getPort()) &&
                DatabaseHelper.updateUserNickname(client.getNickname(), msg.getText(), server.getPort())) {
            client.updateUser();
            sendMessage(msg);
        } else {
            client.sendLocalMessage(createMessage(connectWords(SERVER_NAME, "Impossible change nickname."), server));
        }
    }

    private void executeEndCommand(){
        try {
            server.closeClient(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeGetOnlineCommand(){
        sendMessage(createMessage(connectWords(SERVER_NAME, "Online users: \n" + ChatCommandsHelper.getNicknames(new ArrayList<>(server.getOnlineClients()))), server));
    }

    private void executeDefaultCommand(){
        sendMessage(createMessage(connectWords(SERVER_NAME, "Incorrect command"), server));
    }
}
