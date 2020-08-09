package Database;

import AuthService.User;

import java.sql.*;
import java.util.ArrayList;

import static Database.DatabaseSQLRequests.*;

public class DatabaseHelper {
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Connection connection;
    private static Statement statement;

    public static void createUsersTable() {
        executeUpdate(CREATE_USERS_TABLE);
    }

    public static void reCreateUsersTable() {
        dropUsersTable();
        createUsersTable();
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

    public static boolean updateUserNickname(String userNickname, String userNewNickname) {
        return executeUpdate(getUpdateUserRequest(userNickname, userNewNickname));
    }

    public static boolean updateUserNickname(String login, String password, String userNewNickname) {
        return executeUpdate(getUpdateUserRequest(login, password, userNewNickname));
    }

    public static boolean updateUserIsOnlineStatus(String userNickname, boolean isOnline) {
        return executeUpdate(getUpdateUserRequest(userNickname, isOnline));
    }

    public static boolean deleteUser(String userNickname) {
        return executeUpdate(getDeleteUserRequest(userNickname));
    }

    public static User getUser(String login, String password) {
        try {
            if (connection == null || connection.isClosed()) openConnection();
            var result = executeQuery(getSelectUserRequest(login, password));
            if (result == null) return null;
            var out = getUser(result.getInt("id"));
            if (connection != null || !connection.isClosed()) closeConnection();
            return out;
        } catch (SQLException ex) {
            //ex.printStackTrace();
            return null;
        }
    }

    public static boolean isNicknameFree(String nickname) {
        var users = DatabaseHelper.getAllUsers();
        for (var user : users) {
            if (user.isNicknameCorrect(nickname) && !user.isOnline()) return false;
        }
        return true;
    }

    public static User getUser(int userId) {
        try {
            if (connection == null || connection.isClosed()) openConnection();
            var result = executeQuery(getSelectUserRequest(userId));
            if (result == null) return null;
            var out = new User(result.getString("login"), result.getString("password"), result.getString("nickname"), result.getBoolean("isOnline"));
            out.setId(result.getInt("id"));
            if (connection != null || !connection.isClosed()) closeConnection();
            return out;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ArrayList<User> getAllUsers() {
        try {
            if (connection == null || connection.isClosed()) openConnection();
            var out = new ArrayList<User>();
            var idList = new ArrayList<Integer>();
            var result = executeQuery(SELECT_ALL_USERS);
            if (result == null) return null;
            while (result.next()) {
                idList.add(result.getInt("id"));
            }
            for (var id : idList) {
                out.add(getUser(id));
            }
            if (connection != null || !connection.isClosed()) closeConnection();
            return out;
        } catch (SQLException ex) {
            closeConnection();
            return null;
        }
    }

    private static boolean executeUpdate(String sqlCommand) {
        try {
            if (connection == null || connection.isClosed()) openConnection();
            statement.executeUpdate(sqlCommand);
            closeConnection();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            closeConnection();
        }
        return false;
    }

    private static void openConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            closeConnection();
        }
    }

    private static void closeConnection() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static ResultSet executeQuery(String sqlRequest) {
        try {
            return statement.executeQuery(sqlRequest);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
