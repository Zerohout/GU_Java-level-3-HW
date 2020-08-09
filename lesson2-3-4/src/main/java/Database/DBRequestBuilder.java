package Database;

import java.util.ArrayList;

public class DBRequestBuilder {
    private String select = "SELECT";
    private String fromDb = "FROM users";
    private String where = "WHERE";
    private String insert = "INSERT INTO users";
    private String values = "VALUES";
    private String update = "UPDATE users SET";
    private String delete = "DELETE FROM users";
    private String and = "AND";

    private ArrayList<String> requestParts = new ArrayList<>();
    private ArrayList<String> insertParts = new ArrayList<>();

    public DBRequestBuilder select(String... args) {
        requestParts.add(select);
        if (args == null || args.length == 0 || args[0] == null
                || args[0].equals("") || args[0].equals("*")) {
            requestParts.add("*");
        } else {
            var size = args.length;
            for (var i = 0; i < size; i++) {
                var arg = args[i];
                if (i != size - 1) {
                    arg += ",";
                }
                requestParts.add(arg);
            }
        }
        requestParts.add(fromDb);
        return this;
    }

    public DBRequestBuilder delete() {
        requestParts.add(delete);
        return this;
    }

    //region WHERE
    public <T> DBRequestBuilder where(String key, T value) {
        checkWhere();
        addKeyValue(key, value);
        return this;
    }

    private void checkWhere() {
        if (!requestParts.contains(where)) {
            requestParts.add(where);
            return;
        }
        if (requestParts.indexOf(where) != requestParts.size() - 1) {
            requestParts.add(and);
        }
    }
    //endregion

    //region UPDATE
    public <T> DBRequestBuilder update(String key, T value) {
        checkUpdate();
        if (requestParts.size() >= 2) {
            var index = requestParts.size() - 1;
            var temp = requestParts.get(index);
            temp += ",";
            requestParts.remove(index);
            requestParts.add(temp);
        }

        addKeyValue(key, value);
        return this;
    }

    private void checkUpdate() {
        if (!requestParts.contains(update)) {
            requestParts.add(update);
        }
    }
    //endregion

    //region INSERT
    public <T> DBRequestBuilder insert(String key, T value) {
        checkInsert();
        var valuesIndex = insertParts.indexOf(values);
        if (valuesIndex == insertParts.size() - 1) {
            insertParts.add(valuesIndex, String.format("(%s)", key));
            if (value instanceof String) {
                insertParts.add(String.format("('%s')", value));
            } else if (value instanceof Integer) {
                insertParts.add(String.format("(%d)", value));
            } else if (value instanceof Boolean) {
                insertParts.add(String.format("(%d)", (Boolean) value ? 1 : 0));
            }
        } else {
            var lastKey = insertParts.get(valuesIndex - 1).replace(")", ",");
            insertParts.remove(valuesIndex - 1);
            insertParts.add(valuesIndex - 1, lastKey);
            insertParts.add(valuesIndex, key + ")");

            var lastValue = insertParts.get(insertParts.size() - 1).replace(")", ",");
            insertParts.remove(insertParts.size() - 1);
            insertParts.add(lastValue);
            if (value instanceof String) {
                insertParts.add(String.format("'%s')", value));
            } else if (value instanceof Integer) {
                insertParts.add(String.format("%d)", value));
            } else if (value instanceof Boolean) {
                insertParts.add(String.format("%d)", (Boolean) value ? 1 : 0));
            }
        }
        return this;
    }

    public DBRequestBuilder buildInsert() {
        requestParts.addAll(insertParts);
        return this;
    }

    private void checkInsert() {
        if (!insertParts.contains(insert)) {
            insertParts.add(insert);
            insertParts.add(values);
        }
    }
    //endregion

    public DBRequestBuilder reset() {
        return new DBRequestBuilder();
    }

    public String build() {
        var out = new StringBuilder();
        var size = requestParts.size();
        for (var i = 0; i < size; i++) {
            out.append(requestParts.get(i));
            out.append(i != size - 1 ? " " : ";");
        }
        return out.toString();
    }

    private <T> void addKeyValue(String key, T value) {
        if (value instanceof String) {
            requestParts.add(String.format("%s = '%s'", key, value));
        } else if (value instanceof Integer) {
            requestParts.add(String.format("%s = %d", key, value));
        } else if (value instanceof Boolean) {
            requestParts.add(String.format("%s = %d", key, (Boolean) value ? 1 : 0));
        }
    }

    public static String getDropUsersTableRequest() {
        return "DROP TABLE IF EXISTS users;";
    }

    public static String getCreateUsersTableRequest() {
        return """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,
                login STRING (36) NOT NULL,
                password STRING (36) NOT NULL,
                nickname STRING (36) NOT NULL,
                isOnline BOOLEAN DEFAULT(0) NOT NULL,
                UNIQUE (login, password, nickname));
            """;
    }


}

