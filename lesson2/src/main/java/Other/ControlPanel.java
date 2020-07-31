package Other;

import Client.ClientApp;
import Server.ServerApp;

import javax.swing.*;
import java.awt.*;

import static java.awt.GridBagConstraints.*;

public class ControlPanel extends JFrame {

    private JCheckBox autoAuthChcBox;
    private JButton addClientBtn;
    private JButton openServerBtn;
    private JTextField serverPortTField;
    private int openedClientFramesCount;
    private int port;

    public ControlPanel() {
        prepareGUI();
    }

    private boolean getCHeckBoxStatus() {
        return autoAuthChcBox.isSelected();
    }

    //region GUI methods
    private void prepareGUI() {
        setTitle("Панель управления");
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setWindowSize();

        addComponents();
        setVisible(true);
    }

    private void setWindowSize() {
        Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = sSize.height;
        int width = sSize.width;
        setSize(width / 4, height / 2);
        setLocation((width / 2) - (getWidth() / 2), (height / 2) - (getHeight() / 2));
    }

    private void addComponents() {
        addServerComponents();
        addClientComponents();
    }

    private void addServerComponents() {
        addEmptyRow(0);

        createServerPortTField();
        createOpenServerBtn();

        placeComponent(getContentPane(), new JLabel("Порт сервера"), 0, 1, 1, 50, NONE, CENTER);
        placeComponent(getContentPane(), serverPortTField, 0, 2, 1, 50, HORIZONTAL, CENTER);
        placeComponent(getContentPane(), openServerBtn, 1, 1, 2, 10, NONE, WEST);

        addEmptyRow(3);
    }
    private void addClientComponents() {
        autoAuthChcBox = new JCheckBox();
        createAddClientBtn();

        placeComponent(getContentPane(), createAuthPanel(), 3, 2, HORIZONTAL, SOUTH);
        placeComponent(getContentPane(), addClientBtn, 4, 2, NONE, NORTH);
        addEmptyRow(5);
    }

    //region Create components
    private void createServerPortTField(){
        serverPortTField = new JTextField();
        serverPortTField.setText("8834");
        serverPortTField.setSize(100, 25);
    }

    private JPanel createAuthPanel() {
        var autoAuthPanel = new JPanel();
        autoAuthPanel.setLayout(new FlowLayout());
        autoAuthPanel.add(autoAuthChcBox);
        autoAuthPanel.add(new JLabel("Авто аутентификация клиентов (кроме первого открытого)"));
        return autoAuthPanel;
    }

    private void createOpenServerBtn() {
        openServerBtn = new JButton("Открыть сервер");
        openServerBtn.addActionListener(e -> openServerAction());
    }

    private void createAddClientBtn() {
        addClientBtn = new JButton("Добавить клиента");
        addClientBtn.addActionListener(e -> addClientAction());
        addClientBtn.setEnabled(false);
    }
    //endregion

    //region Buttons actions
    private void openServerAction() {
        try {
            port = Integer.parseInt(serverPortTField.getText());
        } catch (NumberFormatException ex) {
            return;
        }
        new Thread(() -> {
            new ServerApp(this, port);
            setOpenServerBtnStatus(false);
            setAddClientBtnStatus(true);
        }).start();
    }

    private void addClientAction() {
        if (getCHeckBoxStatus()) {
            new Thread(() -> {
                new ClientApp("localhost", port, openedClientFramesCount != 0);
            }).start();
        } else {
            new Thread(() -> {
                new ClientApp("localhost", port, false);
            }).start();
        }
        openedClientFramesCount++;
    }

    public void setOpenServerBtnStatus(boolean isEnabled) {
        setAddClientBtnStatus(!isEnabled);
        serverPortTField.setEnabled(isEnabled);
        openServerBtn.setEnabled(isEnabled);
    }

    private void setAddClientBtnStatus(boolean isEnabled) {
        addClientBtn.setEnabled(isEnabled);
    }
    //endregion

    //endregion

    //region grid methods
    private void placeComponent(Container container, Component component, int gridX, int gridY, int gridHeight, int gridWidth, Insets insets, int fill, int anchor, float weightX) {
        var gbc = new GridBagConstraints();
        gbc.gridx = gridX;
        gbc.gridy = gridY;
        gbc.gridheight = gridHeight;
        gbc.gridwidth = gridWidth;
        gbc.insets = insets;
        gbc.fill = fill;
        gbc.anchor = anchor;
        gbc.weightx = weightX;
        container.add(component, gbc);
    }

    private void placeComponent(Container container, Component component, int gridX, int gridY, int gridHeight, int leftInsets, int fill, int anchor) {
        var gbc = new GridBagConstraints();
        gbc.gridx = gridX;
        gbc.gridy = gridY;
        gbc.gridheight = gridHeight;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, leftInsets, 0, 0);
        gbc.fill = fill;
        gbc.anchor = anchor;
        gbc.weightx = 0.1f;
        container.add(component, gbc);
    }

    private void placeComponent(Container container, Component component, int gridY, int gridWidth, int fill, int anchor) {
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridheight = 1;
        gbc.gridwidth = gridWidth;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.fill = fill;
        gbc.anchor = anchor;
        gbc.weightx = 0;
        container.add(component, gbc);
    }

    private void addEmptyRow(int y) {
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        gbc.fill = BOTH;
        gbc.weighty = 1f;
        getContentPane().add(Box.createGlue(), gbc);
    }
    //endregion
}
