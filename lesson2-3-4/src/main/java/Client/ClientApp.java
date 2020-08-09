package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static Helpers.ChatCommandsHelper.*;
import static Message.MessageBuilder.*;

import Helpers.ChatFrameBase;
import Database.DatabaseHelper;
import Message.MessageBuilder;

import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;

public class ClientApp extends ChatFrameBase {
    private static int clientCounter;
    public static boolean isSupSevMonitors;
    private int frameIndex;
    private boolean isStopped;
    private DataOutputStream out;
    private DataInputStream in;
    private Socket socket;

    public ClientApp(String host, int port, boolean autoAuth) {
        try {
            this.socket = new Socket(host, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            clientCounter++;
            prepareGUI(socket);
            new Thread(this::readMessage).start();
            doAutoAuth(autoAuth);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {
        while (!socket.isClosed()) {
            try {
                var text = in.readUTF();
                var parts = text.split("\\s");

                if (parts[1].startsWith("/")) {
                    executeCommand(parts);
                    continue;
                }
                sendLocalMessage(text);
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        }
    }

    @Override
    protected synchronized void sendMessage(String text) {
        if (text == null || text.isBlank() || text.isEmpty()) return;
        try {
            if (!text.split("\\s")[0].equals(getTitle())) {
                text = String.format("%s %s", getTitle(), text);
            }
            out.writeUTF(text);
        } catch (IOException ex) {
            ex.printStackTrace();
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        } finally {
            msgInputField.setText("");
            msgInputField.grabFocus();
        }
    }

    private void executeCommand(String[] parts) throws IOException {
        switch (parts[1]) {
            case END:
                isStopped = true;
                socket.close();
                this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                break;
            case AUTH_OK:
            case RENAME:
                updateTitle(parts[2]);
                break;
            default:
                break;
        }
    }

    // For debugging
    private void doAutoAuth(boolean isAutoAuth) {
        if (!isAutoAuth || clientCounter == 1) return;
        var login = "login" + clientCounter;
        var password = "pass" + clientCounter;
        var nickname = "Client" + clientCounter;

        var builder = new MessageBuilder();

        if (DatabaseHelper.getUser(login, password) == null) {
            sendMessage(builder.compositeMessage(connectWords(getTitle(), REG, login, password, nickname)).build().getConnectedText());
        }
        sendMessage(builder.compositeMessage(connectWords(getTitle(), AUTH, login, password)).build().getConnectedText());
    }

    //region GUI methods
    private void updateTitle(String title) {
        setVisible(false);
        setTitle(title);
        setVisible(true);
    }

    private synchronized void prepareGUI(Socket socket) {
        setTitle(socket.toString());
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setSize(FrameLocation.getFrameSize());
        frameIndex = FrameLocation.getFrameIndex();
        setLocation(FrameLocation.getFrameLocation());

        addComponents();
        setVisible(true);
    }

    private void addComponents() {
        this.chatArea = new JTextArea();
        this.msgInputField = new JTextField();
        addChatArea(getContentPane(), chatArea);
        addBottomPanel(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                FrameLocation.setFrameStatus(frameIndex, false);
                if (!isStopped) {
                    isStopped = true;
                    sendMessage("/end");
                }
            }
        });
    }
    //endregion

    //region фича с 2-мя мониторами
    private static class FrameLocation {
        private static GraphicsDevice[] monitors;
        private static FrameLocation[] frameLocations;
        private static int openedDefaultFramesCount;
        private int posNumber;
        private boolean isOpen;

        static {
            monitors = getLocalGraphicsEnvironment().getScreenDevices();
            frameLocations = new FrameLocation[10];
            for (var i = 0; i <= 9; i++) {
                frameLocations[i] = new FrameLocation(false, i);
            }
        }

        private FrameLocation(boolean isOpen, int posNumber) {
            this.isOpen = isOpen;
            this.posNumber = posNumber;
        }

        //region public getters and setters
        static Dimension getFrameSize() {
            return isSupSevMonitors ?
                    new Dimension(getScreenSize().width / 4 - 25, getScreenSize().height / 2 - 50)
                    : new Dimension(getScreenSize().width / 3, getScreenSize().height / 2);
        }

        static int getFrameIndex() {
            if (isSupSevMonitors) {
                for (var i = 0; i < frameLocations.length; i++) {
                    if (frameLocations[i].isClose()) return i;
                }
            }
            return 0;
        }

        static Point getFrameLocation() {
            var index = getFrameIndex();
            setFrameStatus(index, true);
            return frameLocations[index].getLocation();
        }

        static void setFrameStatus(int frameIndex, boolean isOpen) {
            if (frameIndex == 0 || frameIndex > 8) {
                if (isOpen) {
                    openedDefaultFramesCount++;
                } else {
                    if (openedDefaultFramesCount > 1) {
                        openedDefaultFramesCount--;
                        return;
                    }
                    openedDefaultFramesCount--;
                }
            }
            frameLocations[frameIndex].isOpen = isOpen;
        }
        //endregion

        //region private getters
        private Point getLocation() {
            if (monitors.length == 1)
                return getDefaultFrameLocation();
            if (posNumber == 0 || posNumber > 8)
                return getDefaultFrameLocation();

            var secondMonitorPosition = monitors[1].getDefaultConfiguration().getBounds().getLocation();
            int numInRow = posNumber > 4 ? 1 : 0;
            var multiplier = posNumber > 4 ? posNumber - 4 : posNumber;

            return new Point(
                    (secondMonitorPosition.x - getFrameSize().width) + ((getFrameSize().width + 25) * multiplier),
                    (getScreenSize().height / 2) * numInRow);
        }

        private Point getDefaultFrameLocation() {
            return new Point(getScreenSize().width - getFrameSize().width - 50,
                    (getScreenSize().height / 2) - (getFrameSize().height / 2));
        }

        private static Dimension getScreenSize() {
            var monitorIndex = monitors.length > 1 ? 1 : 0;
            return monitors[monitorIndex].getDefaultConfiguration().getBounds().getSize();
        }
        //endregion

        private boolean isClose() {
            return !isOpen;
        }
    }
    //endregion
}
