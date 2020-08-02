package Helpers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class ChatFrameBase extends JFrame {
    protected JTextArea chatArea;
    protected JTextField msgInputField;

    //region GUI methods
    protected static void addChatArea(Container container, JTextArea jTArea) {
        jTArea.setEditable(false);
        jTArea.setLineWrap(true);
        container.add(new JScrollPane(jTArea), BorderLayout.CENTER);
    }

    protected void addBottomPanel(Container container) {
        var bottomPanel = new JPanel(new BorderLayout());
        addMsgInputField(bottomPanel, msgInputField);
        addBtnSendMsg(bottomPanel);
        container.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addMsgInputField(JPanel panel, JTextField jTField) {
        jTField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    sendMessage(msgInputField.getText());
                }
            }
        });
        panel.add(jTField, BorderLayout.CENTER);
    }

    private void addBtnSendMsg(JPanel panel) {
        JButton btnSendMsg = new JButton("Отправить");
        panel.add(btnSendMsg, BorderLayout.EAST);
        btnSendMsg.addActionListener(e -> sendMessage(msgInputField.getText()));
    }
    //endregion

    protected String getCurrentDate(){
        var dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        return dateFormat.format(new Date());
    }

    protected abstract void sendMessage(String msg);

    public void sendLocalMessage(String msg) {
        if (chatArea == null) {
            return;
        }
        msg = String.format("%s %s",getCurrentDate(),msg);
        chatArea.append(msg);
        chatArea.append("\n");
    }
}
