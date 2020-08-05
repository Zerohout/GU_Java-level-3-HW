package Server;

import Helpers.ChatFrameBase;
import Helpers.ControlPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static Helpers.ChatCommandsHelper.*;
import static Services.MessageService.*;

public class ServerApp extends ChatFrameBase {
    private ServerHandler server;
    private ControlPanel controlPanel;
    private boolean isStopped;

    public ServerApp(ControlPanel controlPanel, int port) {
        this.controlPanel = controlPanel;
        prepareGUI();
        server = new ServerHandler(port, this);
    }

    @Override
    protected synchronized void sendMessage(String text) {
        if (text == null || text.isEmpty() || text.isBlank()) return;
        var msg = createMessage(connectWords(ServerHandler.SERVER_NAME, text),server);
        server.sendMessage(msg);
        msgInputField.setText("");
        msgInputField.grabFocus();
    }

    public void close() {
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
                controlPanel.setComponentsEnabled(false);
                if(!isStopped){
                    isStopped = true;
                sendMessage(END);}
                super.windowClosing(e);
            }
        });
    }
    //endregion
}
