package Server;

import Helpers.ChatFrameBase;
import Helpers.ControlPanel;
import Message.MessageBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static Helpers.ChatCommandsHelper.END;
import static Message.MessageBuilder.*;

public class ServerApp extends ChatFrameBase {
    private ServerHandler server;
    private ControlPanel controlPanel;
    private boolean isStopped;

    public ServerApp(ControlPanel controlPanel, int port) {
        this.controlPanel = controlPanel;
        prepareGUI();
        server = new ServerHandler(port, this);
        ControlPanel.setCurrentServer(server);
    }

    @Override
    protected synchronized void sendMessage(String text) {
        if (text == null || text.isEmpty() || text.isBlank()) return;
        new MessageBuilder().compositeMessage(connectWords(ServerHandler.SERVER_NAME, text)).setRecipients(server).build().send();
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
