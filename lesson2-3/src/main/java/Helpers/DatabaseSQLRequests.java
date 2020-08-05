package Helpers;

import Entities.User;

public class DatabaseSQLRequests {
    static final String USERS_TABLE = "users";
    static final String CLEAR_USERS_TABLE = "DELETE FROM users;";
    static final String DROP_USERS_TABLE = "DROP TABLE IF EXISTS users;";
    static final String CREATE_USERS_TABLE =
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,
                login STRING (36) NOT NULL,
                password STRING (36) NOT NULL,
                nickname STRING (36) NOT NULL,
                serverPort INTEGER NOT NULL,
                UNIQUE (login, nickname, serverPort));
            """;

    public static String getInsertUserRequest(User user) {
        return String.format("INSERT INTO %s (login, password, nickname, serverPort) VALUES ('%s', '%s', '%s', %d);",
                USERS_TABLE, user.getLogin(), user.getPassword(), user.getNickname(), user.getServerPort());
    }

    public static String getUpdateUserRequest(String userNickname, String userNewNickname, int serverPort) {
        return String.format("UPDATE %s SET nickname = '%s' WHERE nickname = '%s' AND serverPort = %d;",
                USERS_TABLE, userNewNickname, userNickname, serverPort);
    }

    public static String getDeleteUserRequest(String userNickname, int serverPort) {
        return String.format("DELETE FROM %s WHERE nickname = '%s' AND serverPort = %d;",
                USERS_TABLE, userNickname, serverPort);
    }

    public static String getSelectUserRequest(int userId) {
        return String.format("SELECT * FROM %s WHERE id = %d;",
                USERS_TABLE, userId);
    }

    public static String getSelectUserRequest(String login, String password, int serverPort) {
        return String.format("SELECT * FROM %s WHERE login = '%s' AND password = '%s' AND serverPort = %d;",
                USERS_TABLE, login, password, serverPort);
    }

    public static String getSelectAllUsersOnServerRequest(int serverPort) {
        return String.format("SELECT * FROM %s WHERE serverPort = %d;", USERS_TABLE, serverPort);
    }
}
