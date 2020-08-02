package Helpers;

import java.util.ArrayList;

public class ChatCommandsHelper {
    public static final String HELP = "/help";
    public static final String GET_COMMS = "/getcomms";
    public static final String AUTH = "/auth";
    public static final String REG = "/reg";
    public static final String LOGOUT = "/logout";
    public static final String PRIVATE_MSG = "/t";
    public static final String GET_ONLINE = "/getonline";
    public static final String RENAME = "/rename";
    public static final String END = "/end";
    public static final String DELETE = "/delete";
    public static final String AUTH_OK = "/authok";
    public static final String SYS_TITLE = "//title";
    public static final String SYS_UNKNOWN_COMM = "//?";

    public static ArrayList<String> getAllClientCommands() {
        var out = new ArrayList<String>();
        out.add(HELP);
        out.add(GET_COMMS);
        out.add(AUTH);
        out.add(REG);
        out.add(LOGOUT);
        out.add(PRIVATE_MSG);
        out.add(GET_ONLINE);
        out.add(RENAME);
        out.add(END);
        return out;
    }

    public static ArrayList<String> getAllServerCommands() {
        var out = new ArrayList<String>();
        out.add(DELETE);
        return out;
    }

    public static String getCommandHelp(String command) {
        switch (command) {
            case AUTH:
                return String.format("\"%s login password\" for login into chat.", AUTH);
            case REG:
                return String.format("\"%s login password nickname\" for registration into chat.", REG);
            case LOGOUT:
                return String.format("\"%s\" for logout from chat.", LOGOUT);
            case PRIVATE_MSG:
                return String.format("\"%s recipient_nickname\" for sending private message to recipient.", PRIVATE_MSG);
            case GET_ONLINE:
                return String.format("\"%s\" for getting list of all online users.", GET_ONLINE);
            case RENAME:
                return String.format("\"%s new_nickname\" for renaming your nickname.", RENAME);
            case END:
                return String.format("\"%s\" for exiting from chat.", END);
            case DELETE:
                return String.format("\"%s nickname\" for deleting user from DB and close his client application", DELETE);
            case GET_COMMS:
                return String.format("\"%s\" for getting full list of available commands", GET_COMMS);
            case HELP:
                return String.format("\"%s command\" for getting command description or \"%s\" for getting full list of available commands",
                        HELP, GET_COMMS);
            default:
                return "Unknown command";
        }
    }
}
