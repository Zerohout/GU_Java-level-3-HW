package Server;

import Other.ChatBase;
import Other.ControlPanel;
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

    private void sendServerMessage(String msg) {
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
        JPanel bottomPanel = new JPanel(new BorderLayout());
        addMsgInputField(bottomPanel,msgInputField, () -> sendServerMessage(msgInputField.getText()));
        addBtnSendMsg(bottomPanel, () -> sendServerMessage(msgInputField.getText()));
        add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isWorking = false;
                super.windowClosing(e);
                controlPanel.setOpenServerBtnStatus(true);
                sendServerMessage("/end");
            }
        });
    }

    public boolean isWorking() {
        return isWorking;
    }
    //endregion
}
