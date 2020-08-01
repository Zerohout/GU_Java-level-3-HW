package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import Helpers.ChatFrameBase;

import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;

public class ClientApp extends ChatFrameBase {
    private static int clientCounter;
    private int frameIndex;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean isWorking = true;
    public static boolean isSupSevMonitors;

    public ClientApp(String host, int port, boolean autoAuth) {
        try {
            this.socket = new Socket(host, port);
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            clientCounter++;
            prepareGUI(socket);
            new Thread(this::readMessage).start();
            doAutoAuth(autoAuth);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //region Text methods
    private void readMessage() {
        while (isWorking) {
            String msg;
            try {
                msg = in.readUTF();
                if (msg.startsWith("/")) commandListener(msg);
                else sendLocalMessage(msg);
            } catch (IOException e) {
                //e.printStackTrace();
                close();
                return;
            }
        }
    }

    @Override
    protected synchronized void sendMessage(String msg) {
        if (msg.startsWith("/")) {
            try {
                commandListener(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                out.writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        msgInputField.setText("");
        msgInputField.grabFocus();
    }

    private void commandListener(String msg) throws IOException {
        if (msg.startsWith("//")) {
            updateTitle(msg);
        } else if (msg.startsWith("/end")) {
            if (socket.isClosed()) {
                close();
            } else {
                out.writeUTF("/end");
            }
        } else {
            out.writeUTF(msg);
        }
    }

    private void updateTitle(String msg) {
        setTitle(msg.replace("//", ""));
        setVisible(false);
        setVisible(true);
    }

    private void doAutoAuth(boolean isAutoAuth) {
        if (!isAutoAuth) return;
        sendMessage(String.format("/reg client#%1$d 1234 client#%1$d", clientCounter));
        sendMessage(String.format("/auth client#%d 1234", clientCounter));
    }

    //endregion

    private void close() {
        isWorking = false;
        try {
            in.close();
            out.close();
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }

    //region GUI methods
    private synchronized void prepareGUI(Socket socket) {
        setTitle("Клиент " + socket.toString());
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
                try {
                    if (!socket.isClosed()) out.writeUTF("/end");
                } catch (IOException ex) {
                    ex.printStackTrace();
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
        public static Dimension getFrameSize() {
            return isSupSevMonitors ?
                    new Dimension(getScreenSize().width / 4 - 25, getScreenSize().height / 2 - 50)
                    : new Dimension(getScreenSize().width / 3, getScreenSize().height / 2);
        }

        public static int getFrameIndex() {
            if (isSupSevMonitors) {
                for (var i = 0; i < frameLocations.length; i++) {
                    if (frameLocations[i].isClose()) return i;
                }
            }
            return 0;
        }

        public static Point getFrameLocation() {
            var index = getFrameIndex();
            setFrameStatus(index, true);
            return frameLocations[index].getLocation();
        }

        public static void setFrameStatus(int frameIndex, boolean isOpen) {
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
