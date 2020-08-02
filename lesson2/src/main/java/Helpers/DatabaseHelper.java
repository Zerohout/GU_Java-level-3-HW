package Helpers;

import AuthService.User;

import java.sql.*;
import java.util.ArrayList;

import static Helpers.DatabaseSQLRequests.*;

public class DatabaseHelper {
    private static Connection connection;
    private static Statement statement;

    public static void createUsersTable() {
        executeUpdate(CREATE_USERS_TABLE);
    }

    public static void dropUsersTable() {
        executeUpdate(DROP_USERS_TABLE);
    }

    public static void clearUsersTable() {
        executeUpdate(CLEAR_USERS_TABLE);
    }

    public static boolean insertUser(User user) {
        return executeUpdate(getInsertUserRequest(user));
    }

    public static boolean updateUserNickname(String userNickname, String userNewNickname, int serverPort) {
        return executeUpdate(getUpdateUserRequest(userNickname, userNewNickname, serverPort));
    }

    public static boolean deleteUser(String userNickname, int serverPort) {
        return executeUpdate(getDeleteUserRequest(userNickname,serverPort));
    }

    public static User getUser(String login, String password, int serverPort) {
        try {
            var result = statement.executeQuery(getSelectUserRequest(login, password, serverPort));
            return getUser(result.getInt("id"));
        } catch (SQLException ex) {
            return null;
        }
    }

    public static User getUser(int userId) {
        try {
            var result = statement.executeQuery(getSelectUserRequest(userId));
            var out = new User(result.getString("login"), result.getString("password"), result.getString("nickname"), result.getInt("serverPort"));
            out.setId(result.getInt("id"));
            return out;
        } catch (SQLException ex) {
            return null;
        }
    }

    public static ArrayList<User> getAllUsers(int serverPort) {
        try {
            var out = new ArrayList<User>();
            var result = statement.executeQuery(getSelectAllUsersOnServerRequest(serverPort));
            while (result.next()) {
                out.add(getUser(result.getInt("id")));
            }
            return out;
        } catch (SQLException ex) {
            return null;
        }
    }

    private static boolean executeUpdate(String sqlCommand) {
        try {
            statement.executeUpdate(sqlCommand);
            return true;
        } catch (SQLException ex) {
           return false;
        }
    }

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
            statement = connection.createStatement();
            createUsersTable();
        } catch (Exception ex) {
            ex.printStackTrace();
            disconnect();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
