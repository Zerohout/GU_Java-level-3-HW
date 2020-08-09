package Database;

import AuthService.User;

public class DatabaseSQLRequests {
    static final String USERS_TABLE = "users";
    static final String CLEAR_USERS_TABLE = "DELETE FROM users;";
    static final String DROP_USERS_TABLE = "DROP TABLE IF EXISTS users;";
    static final String SELECT_ALL_USERS = "SELECT * FROM users;";
    static final String CREATE_USERS_TABLE =
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,
                login STRING (36) NOT NULL,
                password STRING (36) NOT NULL,
                nickname STRING (36) NOT NULL,
                isOnline BOOLEAN DEFAULT(0) NOT NULL,
                UNIQUE (login, password, nickname));
            """;

    public static String getInsertUserRequest(User user) {
        return String.format("INSERT INTO users (login, password, nickname) VALUES ('%s', '%s', '%s');",
                user.getLogin(), user.getPassword(), user.getNickname());
    }

   public static String getUpdateUserRequest(String userNickname, String userNewNickname) {
        return String.format("UPDATE users SET nickname = '%s' WHERE nickname = '%s';",
                userNewNickname, userNickname);
    }

    public static String getUpdateUserRequest(String login, String password, String userNewNickname) {
        return String.format("UPDATE users SET nickname = '%s' WHERE login = '%s' AND password = '%s';", userNewNickname, login, password);
    }

    public static String getUpdateUserRequest(String userNickname, boolean isOnline) {
        return String.format("UPDATE users SET isOnline = %d WHERE nickname = '%s';", isOnline ? 1 : 0, userNickname);
    }

    public static String getDeleteUserRequest(String userNickname) {
        return String.format("DELETE FROM users WHERE nickname = '%s';", userNickname);
    }

    public static String getSelectUserRequest(int userId) {
        return String.format("SELECT * FROM users WHERE id = %d;", userId);
    }

    public static String getSelectUserRequest(String login, String password) {
        return String.format("SELECT * FROM users WHERE login = '%s' AND password = '%s';",
                login, password);
    }
}
