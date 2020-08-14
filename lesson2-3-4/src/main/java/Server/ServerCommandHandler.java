package Server;

import Database.DatabaseHelper;
import Message.Message;
import Message.MessageBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static Helpers.ChatCommandsHelper.*;
import static Message.MessageBuilder.*;
import static Server.ServerHandler.SERVER_NAME;
import static Server.ServerHandler.getClientByNickname;

public class ServerCommandHandler {
    private ServerHandler server;
    private MessageBuilder mb;
    private static final Logger logger = LogManager.getLogger(ServerCommandHandler.class);

    public ServerCommandHandler(ServerHandler server) {
        mb = new MessageBuilder();
        this.server = server;
    }

    synchronized void serverCommandListener(Message msg) {
        logger.info("Сообщение {} принято на обработку в качестве команды", msg.getConnectedText());
        switch (msg.getCommand()) {
            case HELP -> executeHelpCommand(msg);
            case GET_COMMS -> executeGetCommsCommand();
            case AUTH, REG, LOGOUT, RENAME -> executeUnneededCommands();
            case PRIVATE_MSG -> executePrivateMsgCommand(msg);
            case GET_ONLINE -> executeGetOnline();
            case END -> executeEndCommand();
            case DELETE -> executeDeleteCommand(msg);
            default -> {
                var text = "Incorrect command";
                sendServerSystemMessage(text);
                logger.warn("Команда была не определена. Отправлено сообщение серверу: {}", text);
            }
        }
    }

    // SERVER /help
    // SERVER /help (0)/command
    private void executeHelpCommand(Message msg) {
        var args = msg.getCommandArgs();
        var text = getCommandHelp(args.length == 0 ? msg.getCommand() : args[0]);
        sendServerSystemMessage(text);
        logger.info("Сообщение определено как команда {}. Отправлено системное сообщение серверу: {}.", msg.getCommand(), text);
    }

    // SERVER /getcomms
    private void executeGetCommsCommand() {
        var commands = getAllClientCommands();
        commands.addAll(getAllServerCommands());
        var text = commands.toString();
        sendServerSystemMessage(text);
        logger.info("Команда определена как /getcomms. Отправлено сообщение серверу: {}", text);
    }

    private void executeUnneededCommands() {
        var text = "You don't need this commands";
        sendServerSystemMessage(text);
        logger.info("Сообщение определено как unneeded команда. Отправлено системное сообщение серверу: {}.", text);
    }

    // SERVER /t (0)recipient (1...)text
    // SERVER /t (0)recipient (1)/command (2...)args
    // SERVER /t (0)recipient (1)/t (2)/recipient (3...)text
    private void executePrivateMsgCommand(Message msg) {
        var args = msg.getCommandArgs();
        if (args.length < 2) {
            var text = "Incorrect command";
            sendServerSystemMessage(text);
            logger.warn("Сообщение определено как incorrect команда. Отправлено системное сообщение серверу: {}.", text);
            return;
        }
        var recipient = getClientByNickname(args[0]);
        if (recipient == null) {
            var text = "Nickname not found";
            sendServerSystemMessage(text);
            logger.warn("Получатель приватного сообщения {} не найден. Отправлено системное сообщение серверу: {}.", args[0], text);
            return;
        }
        if (args[1].startsWith("/")) {
            var commMsg = mb.reset().compositeMessage(connectParts(args, 0)).setRecipients(recipient).build();
            commMsg.send();
            logger.info("Сообщение {} отправлено получателю {} как команда от лица получателя.", commMsg.getConnectedText(), recipient.getName());
        } else {
            var privMsg = mb.reset().isCommand(true).isPrivate(true).setSender(SERVER_NAME)
                    .setRecipients(server, recipient).setText(connectParts(args, 1)).build();
            privMsg.send();
            logger.info("Отправлено приватное сообщение {} серверу и получателю {}", privMsg.getConnectedText(), recipient.getName());
        }
    }

    // SERVER /delete (0)nickname
    private void executeDeleteCommand(Message msg) {
        var args = msg.getCommandArgs();
        if (args.length == 0) {
            var text = "Incorrect command";
            sendServerSystemMessage(text);
            logger.warn("Сообщение определено как incorrect команда. Отправлено системное сообщение серверу: {}.", text);
            return;
        }
        var nickname = args[0];
        logger.info("Команда распознана как /delete");

        var client = getClientByNickname(nickname);
        if (DatabaseHelper.deleteUser(nickname)) {
            logger.info("Клиент {} удалён из БД", nickname);
            sendServerSystemMessage(String.format("User \"%s\" is deleted.", nickname));
            if (client != null) {
                mb.reset().compositeMessage(END).setRecipients(client).build().send();
                logger.info("Клиент {} находился онлайн и был закрыт сервером", nickname);
            }
            return;
        }
        var text = "User not found";
        sendServerSystemMessage(text);
        logger.info("Клиент {} был не найден. Отправлено сообщение серверу {}", nickname, text);
    }

    private void executeGetOnline() {
        var text = "Online users: \n" + server.getNicknames();
        sendServerSystemMessage(text);
        logger.info("Команда определена как /getonline. Серверу отправлено сообщение {}", text);
    }

    private void executeEndCommand() {
        logger.info("Команда была определена как /end. Отправлен запрос остановки сервера.");
        server.stopServer();
    }

    private void sendServerSystemMessage(String text) {
        mb.reset().setServerSystemMessage(text).setRecipients(server).build().send();
    }
}

