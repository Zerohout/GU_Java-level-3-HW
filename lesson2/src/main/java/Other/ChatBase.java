package Other;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public abstract class ChatBase extends JFrame {
    protected JTextArea chatArea;
    protected JTextField msgInputField;

    public static void addChatArea(Container container, JTextArea jTArea){
        jTArea.setEditable(false);
        jTArea.setLineWrap(true);
        container.add(new JScrollPane(jTArea), BorderLayout.CENTER);
    }

    protected void addMsgInputField(JPanel panel, JTextField jTField, ISendMessage iSendMessage){
        jTField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    iSendMessage.sendMessage();
                }
            }});
        panel.add(jTField, BorderLayout.CENTER);
    }

    protected void addBtnSendMsg(JPanel panel, ISendMessage iSendMessage) {
        JButton btnSendMsg = new JButton("Отправить");
        panel.add(btnSendMsg, BorderLayout.EAST);
        btnSendMsg.addActionListener(e -> iSendMessage.sendMessage());
    }

    protected interface ISendMessage{
        void sendMessage();
    }

    public void sendLocalMessage(String msg) {
        if(chatArea == null) return;
        chatArea.append(msg);
        chatArea.append("\n");
    }
}
