package Helpers;

import Client.ClientApp;
import Database.DatabaseHelper;
import Server.ServerApp;
import Server.ServerHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.awt.GridBagConstraints.*;

public class ControlPanel extends JFrame {
    private static ServerHandler currentServer;
    private JCheckBox autoAuthChcBox;
    private JCheckBox supSevMonitorsChcBox;
    private JButton createClientBtn;
    private JButton openServerBtn;
    private JTextField serverPortTField;
    private ServerApp server;
    private ExecutorService executorService;

    private int openedClientFramesCount;
    private int port;
    private int monitorsCount = getLocalGraphicsEnvironment().getScreenDevices().length;

    public ControlPanel() {
        executorService = Executors.newFixedThreadPool(5);
        prepareGUI();
        DatabaseHelper.createUsersTable();
    }

    public static ServerHandler getCurrentServer() {
        return currentServer;
    }

    public static void setCurrentServer(ServerHandler currentServer) {
        ControlPanel.currentServer = currentServer;
    }

    private boolean getAutoAuthChcBoxStatus() {
        return autoAuthChcBox.isSelected();
    }

    private boolean getSupSevMonitorsChcBoxStatus() {
        return supSevMonitorsChcBox.isSelected();
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
        setComponentsEnabled(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (server != null) server.close();
                executorService.shutdown();
                super.windowClosing(e);
            }
        });
    }

    //region Adding server components
    private void addServerComponents() {
        addEmptyRow(0);

        placeComponent(getContentPane(), new JLabel("Порт сервера"), 0, 1, 1, 50, NONE, CENTER);
        addServerPortTField();
        addOpenServerButton();

        addEmptyRow(3);
    }

    private void addServerPortTField() {
        serverPortTField = new JTextField();
        serverPortTField.setText("8834");
        serverPortTField.setSize(100, 25);
        placeComponent(getContentPane(), serverPortTField, 0, 2, 1, 50, HORIZONTAL, CENTER);
    }

    private void addOpenServerButton() {
        openServerBtn = new JButton("Открыть сервер");
        openServerBtn.addActionListener(e -> openServerAction());
        placeComponent(getContentPane(), openServerBtn, 1, 1, 2, 10, NONE, WEST);
    }
    //endregion

    //region Adding client components
    private void addClientComponents() {
        addSupSevMonitorsChcBox();
        addAutoAuthPanel();
        addCreateClientButton();
        addEmptyRow(6);
    }

    private void addSupSevMonitorsChcBox() {
        if (monitorsCount > 1) {
            supSevMonitorsChcBox = new JCheckBox();
            supSevMonitorsChcBox.setSelected(true);

            var supSevMonitors = new JPanel();
            supSevMonitors.setLayout(new FlowLayout());
            supSevMonitors.add(supSevMonitorsChcBox);
            supSevMonitors.add(new JLabel("Включить поддержку 2-х мониторов"));

            placeComponent(getContentPane(), supSevMonitors, 3, 2, HORIZONTAL, SOUTH);
        }
    }

    private void addCreateClientButton() {
        createClientBtn = new JButton("Добавить клиента");
        createClientBtn.addActionListener(e -> createClientAction());
        placeComponent(getContentPane(), createClientBtn, 5, 2, NONE, NORTH);
    }

    private void addAutoAuthPanel() {
        autoAuthChcBox = new JCheckBox();
        var autoAuthPanel = new JPanel();

        autoAuthPanel.setLayout(new FlowLayout());
        autoAuthPanel.add(autoAuthChcBox);
        autoAuthPanel.add(new JLabel("Авто аутентификация клиентов (кроме первого)"));

        placeComponent(getContentPane(), autoAuthPanel, 4, 2, HORIZONTAL, SOUTH);
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
            server = new ServerApp(this, port);
            setComponentsEnabled(true);
        }).start();
    }

    private void createClientAction() {
        createClientBtn.setEnabled(false);
        if (supSevMonitorsChcBox != null && supSevMonitorsChcBox.isEnabled()) {
            ClientApp.isSupSevMonitors = getSupSevMonitorsChcBoxStatus();
            supSevMonitorsChcBox.setEnabled(false);
        }

        executorService.execute(() -> new ClientApp("localhost", port, getAutoAuthChcBoxStatus() && openedClientFramesCount > 1));

        openedClientFramesCount++;
        createClientBtn.setEnabled(true);
    }

    public void setComponentsEnabled(boolean isEnabled) {
        createClientBtn.setEnabled(isEnabled);
        autoAuthChcBox.setEnabled(isEnabled);
        if (supSevMonitorsChcBox != null) supSevMonitorsChcBox.setEnabled(isEnabled);
        serverPortTField.setEnabled(!isEnabled);
        openServerBtn.setEnabled(!isEnabled);
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
