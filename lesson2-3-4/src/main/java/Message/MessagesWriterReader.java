package Message;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessagesWriterReader {
    public synchronized static ArrayList<String> getMessagesFromFile(String login) {
        var file = new File(String.format("./messages/%s/", login));
        file.mkdirs();
        var fileList = file.listFiles();
        if (fileList == null || fileList.length == 0) {
            return new ArrayList<>();
        }
        if (fileList.length == 1) {
            return readFromFile(fileList[0].getPath(), false);
        }
        var lastIndex = fileList.length - 1;
        var parts = fileList[lastIndex].getName().split("_");
        if (Integer.parseInt(parts[1]) == 100) {
            return readFromFile(fileList[lastIndex].getPath(), false);
        } else {
            var messages = readFromFile(fileList[lastIndex].getPath(), false);
            if (messages.size() == 100) {
                return messages;
            } else {
                var tempMessages = readFromFile(fileList[lastIndex - 1].getPath(), false);
                tempMessages.addAll(messages);
                var size = tempMessages.size();
                if (size > 100) {
                    tempMessages.subList(0, size - 100).clear();
                }
                return tempMessages;
            }
        }
    }

    public synchronized static void addMessagesToFile(String login, ArrayList<String> messages) {
        if(messages == null || messages.size() == 0) return;
        var file = new File(String.format("./messages/%s/", login));
        file.mkdirs();
        var fileList = file.listFiles();
        if (fileList == null || fileList.length == 0) {
            writeToFile(String.format("%s\\%d_%d_%s_%s.msg", file.getPath(), 1, messages.size(), login, getDate()), messages);
            return;
        }

        var lastIndex = fileList.length - 1;
        var parts = fileList[lastIndex].getName().split("_");
        var fileNum = Integer.parseInt(parts[0]);
        if (Integer.parseInt(parts[1]) == 100) {
            writeToFile(String.format("%s\\%d_%d_%s_%s.msg", file.getPath(), fileNum + 1, messages.size(), login, getDate()), messages);
        } else {
            var tempMessages = readFromFile(fileList[lastIndex].getPath(), true);
            for (int i = tempMessages.size(), j = messages.size(); i < 100 && j > 0; i++, j--) {
                tempMessages.add(messages.get(0));
                messages.remove(0);
            }
            if (tempMessages.size() == 100) {
                writeToFile(String.format("%s\\%d_%d_%s_%s.msg", file.getPath(), fileNum, 100, login, getDate()), tempMessages);
                if (messages.size() > 0) {
                    writeToFile(String.format("%s\\%d_%d_%s_%s.msg", file.getPath(), fileNum + 1, messages.size(), login, getDate()), messages);
                }
                return;
            }
            writeToFile(String.format("%s\\%d_%d_%s_%s.msg", file.getPath(), fileNum, tempMessages.size(), login, getDate()), tempMessages);
        }
    }

    private static void writeToFile(String path, ArrayList<String> messages) {
        try {
            new File(path).createNewFile();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        try (var oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(messages);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static ArrayList<String> readFromFile(String path, boolean isDeleteFile) {
        var out = new ArrayList<String>();
        try (var oos = new ObjectInputStream(new FileInputStream(path))) {
            out = (ArrayList<String>) oos.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
        if (isDeleteFile) new File(path).delete();
        return out;
    }

    private static String getDate() {
        return new SimpleDateFormat("dd.MM.yy").format(new Date());
    }
}
