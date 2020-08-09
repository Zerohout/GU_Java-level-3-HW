package Database;

import AuthService.User;

import java.sql.*;
import java.util.ArrayList;

import static Database.DBRequestBuilder.*;

public class DatabaseHelper {
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        dbr = new DBRequestBuilder();
    }


    private static Connection connection;
    private static Statement statement;
    private static DBRequestBuilder dbr;

    public static void createUsersTable() {
        executeUpdate(getCreateUsersTableRequest());
    }

    public static void reCreateUsersTable() {
        dropUsersTable();
        createUsersTable();
    }

    public static void dropUsersTable() {
        executeUpdate(getDropUsersTableRequest());
    }

    public static void clearUsersTable() {
        executeUpdate(dbr.reset().delete().build());
    }

    public static boolean insertUser(User user) {
        return executeUpdate(dbr.reset().insert("login", user.getLogin()).insert("password", user.getPassword())
                .insert("nickname", user.getNickname()).buildInsert().build());
    }

    public static boolean updateUserNickname(String userNickname, String userNewNickname) {
        return executeUpdate(dbr.reset().update("nickname", userNewNickname).where("nickname", userNickname).build());
    }

    public static boolean updateUserNickname(String login, String password, String userNewNickname) {
        return executeUpdate(dbr.reset().update("nickname", userNewNickname)
                .where("login", login).where("password", password).build());
    }

    public static boolean updateUserIsOnlineStatus(String userNickname, boolean isOnline) {
        return executeUpdate(dbr.reset().update("isOnline", isOnline).where("nickname", userNickname).build());
    }

    public static boolean deleteUser(String userNickname) {
        return executeUpdate(dbr.reset().delete().where("nickname", userNickname).build());
    }

    public static User getUser(String login, String password) {
        try {
            if (connection == null || connection.isClosed()) openConnection();
            var result = executeQuery(dbr.reset().select().where("login", login).where("password", password).build());
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

    public static void setUsersStatusToOffline() {
        executeUpdate(dbr.reset().update("isOnline", false).where("isOnline", true).build());
    }

    public static User getUser(int userId) {
        try {
            //if (connection == null || connection.isClosed()) openConnection();
            var result = executeQuery(dbr.reset().select().where("id", userId).build());
            if (result == null) return null;
            var out = new User(result.getString("login"), result.getString("password"), result.getString("nickname"), result.getBoolean("isOnline"));
            out.setId(result.getInt("id"));
           // if (connection != null || !connection.isClosed()) closeConnection();
            return out;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }


    public static ArrayList<User> getAllUsers() {
        return getUsers(dbr.reset().select().build());
    }

    public static ArrayList<User> getOnlineUsers() {
        return getUsers(dbr.reset().select().where("isOnline", true).build());
    }

    private static ArrayList<User> getUsers(String request) {
        try {
            if (connection == null || connection.isClosed()) openConnection();
            var out = new ArrayList<User>();
            var idList = new ArrayList<Integer>();
            var result = executeQuery(request);
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

    public static void closeConnection() {
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
