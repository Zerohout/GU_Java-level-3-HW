package Server;

import Helpers.ChatBase;
import Helpers.ControlPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ServerApp extends ChatBase {
    private Server server;
    private ControlPanel controlPanel;
    private boolean isWorking = true;

    public ServerApp(ControlPanel controlPanel, int port) {
        this.controlPanel = controlPanel;
        prepareGUI();
        server = new Server(port, this);
    }

    @Override
    protected synchronized void sendMessage(String msg) {
        server.sendServerMessage(msg);
        msgInputField.setText("");
        msgInputField.grabFocus();
    }

    public void close() {
        isWorking = false;
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    //region GUI methods
    private void prepareGUI() {
        setTitle("Сервер");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        setWindowSize();
        addComponents();
        setVisible(true);
    }

    private void setWindowSize() {
        Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = sSize.height;
        int width = sSize.width;
        setSize(width / 3, height / 2);
        setLocation(50, (height / 2) - (getHeight() / 2));
    }

    private void addComponents() {
        this.chatArea = new JTextArea();
        this.msgInputField = new JTextField();
        addChatArea(this, chatArea);
        addBottomPanel(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controlPanel.setSupSevMonitorsChcBoxEnabled(true);
                isWorking = false;
                super.windowClosing(e);
                controlPanel.setComponentsEnabled(false);
                sendMessage("/end");
            }
        });
    }

    public boolean isWorking() {
        return isWorking;
    }
    //endregion
}
