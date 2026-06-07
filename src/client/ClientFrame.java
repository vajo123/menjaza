package client;

import model.ExchangeInfo;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class ClientFrame extends JFrame {

    private JTextField usernameField;
    private JButton connectButton;
    private JButton deleteButton;
    private JButton exchangesButton;
    private JButton sendRequestButton;
    private JComboBox<ExchangeInfo> exchangeCombo;
    private JTextArea detailsArea;
    private JPanel duplicatesPanel;
    private JPanel missingPanel;
    private HashMap<Integer, JCheckBox> duplicateBoxes;
    private HashMap<Integer, JCheckBox> missingBoxes;

    public ClientFrame() {
        initializeGUI();
        initializeCheckBoxes();
        setVisible(true);
    }

    private void initializeGUI() {
        setTitle("Menjaza");
        setSize(1300, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        usernameField = new JTextField(10);
        connectButton = new JButton("Poveai");
        deleteButton = new JButton("Obrisi");
        exchangesButton = new JButton("Moguce razmene");
        sendRequestButton = new JButton("Posalji zahtev");
        exchangeCombo = new JComboBox<>();

        top.add(new JLabel("Username"));
        top.add(usernameField);
        top.add(connectButton);
        top.add(deleteButton);
        top.add(exchangesButton);
        top.add(exchangeCombo);
        top.add(sendRequestButton);
        add(top, BorderLayout.NORTH);

        duplicatesPanel = new JPanel(new GridLayout(11, 9));
        missingPanel = new JPanel(new GridLayout(11, 9));
        duplicatesPanel.setBorder(BorderFactory.createTitledBorder("Slicice koje imam"));
        missingPanel.setBorder(BorderFactory.createTitledBorder("Slicice koje mi trebaju"));

        JPanel center = new JPanel(new GridLayout(1, 2));
        center.add(new JScrollPane(duplicatesPanel));
        center.add(new JScrollPane(missingPanel));
        add(center, BorderLayout.CENTER);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        add(new JScrollPane(detailsArea), BorderLayout.SOUTH);

        duplicateBoxes = new HashMap<>();
        missingBoxes = new HashMap<>();
    }

    private void initializeCheckBoxes() {
        for (int i = 1; i <= 99; i++) {
            JCheckBox dup = new JCheckBox(String.valueOf(i));
            JCheckBox miss = new JCheckBox(String.valueOf(i));
            duplicateBoxes.put(i, dup);
            missingBoxes.put(i, miss);
            duplicatesPanel.add(dup);
            missingPanel.add(miss);
        }
    }
}