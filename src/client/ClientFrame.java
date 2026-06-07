package client;

import model.*;
import util.StickerGenerator;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;

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
    private UserData user;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientFrame() {
        initializeGUI();
        initializeConnection();
        initializeCheckBoxes();
        startListening();
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

        connectButton.addActionListener(e -> connectUser());
        deleteButton.addActionListener(e -> deleteSelected());
        exchangesButton.addActionListener(e -> loadExchanges());
        exchangeCombo.addActionListener(e -> showExchangeDetails());
    }

    private void initializeConnection() {
        try {
            socket = new Socket("localhost", 9000);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ne mogu da se povezem sa serverom. Proverite da li je server pokrenut.", "Greska", JOptionPane.ERROR_MESSAGE);
        }
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

    private void connectUser() {
        if (user != null) {
            JOptionPane.showMessageDialog(this, "Vec ste prijavljeni kao: " + user.getUsername());
            return;
        }
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Unesite korisnicko ime.");
            return;
        }
        if (out == null) {
            JOptionPane.showMessageDialog(this, "Nema veze sa serverom.");
            return;
        }
        try {
            user = new UserData(username);
            user.setDuplicates(StickerGenerator.generateDuplicates(15));
            user.setMissing(StickerGenerator.generateMissing(user.getDuplicates(), 15));
            refreshCheckBoxStates();
            out.reset();
            out.writeObject(user);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshCheckBoxStates() {
        for (int i = 1; i <= 99; i++) {
            boolean isDuplicate = user.getDuplicates().contains(i);
            boolean isMissing   = user.getMissing().contains(i);

            JCheckBox dupBox  = duplicateBoxes.get(i);
            JCheckBox missBox = missingBoxes.get(i);

            dupBox.setFont(dupBox.getFont().deriveFont(isDuplicate ? Font.BOLD : Font.PLAIN));
            dupBox.setForeground(isDuplicate ? Color.BLUE : Color.LIGHT_GRAY);

            missBox.setFont(missBox.getFont().deriveFont(isMissing ? Font.BOLD : Font.PLAIN));
            missBox.setForeground(isMissing ? Color.BLUE : Color.LIGHT_GRAY);

            dupBox.setSelected(false);
            missBox.setSelected(false);
        }
    }

    private void startListening() {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof List<?>) {
                        List<?> list = (List<?>) obj;
                        if (!list.isEmpty() && list.get(0) instanceof ExchangeInfo) {
                            SwingUtilities.invokeLater(() -> {
                                exchangeCombo.removeAllItems();
                                for (Object o : list) exchangeCombo.addItem((ExchangeInfo) o);
                            });
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Veza prekinuta.");
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void deleteSelected() {
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Niste prijavljeni.");
            return;
        }
        for (int i = 1; i <= 99; i++) {
            if (duplicateBoxes.get(i).isSelected()) user.getDuplicates().remove(i);
            if (missingBoxes.get(i).isSelected())   user.getMissing().remove(i);
        }
        refreshCheckBoxStates();
        try {
            out.reset();
            out.writeObject(user);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadExchanges() {
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Niste prijavljeni.");
            return;
        }
        try {
            exchangeCombo.removeAllItems();
            out.reset();
            out.writeObject("GET_EXCHANGES");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showExchangeDetails() {
        ExchangeInfo info = (ExchangeInfo) exchangeCombo.getSelectedItem();
        if (info == null) {
            return;
        }
        detailsArea.setText("Mozes da menjas slicice sa korisnikom " + info.getOtherUser() + "\n\nTi imas za njega: " + info.getIGive() + "\n\nOn za tebe ima: " + info.getHeGives());
    }
}