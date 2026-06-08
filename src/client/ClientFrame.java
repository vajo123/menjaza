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
        connectButton = new JButton("Povezi");
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
        sendRequestButton.addActionListener(e -> sendExchangeRequest());
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

            Set<Integer> duplicates = StickerGenerator.generateDuplicates(15);
            Set<Integer> missing = StickerGenerator.generateMissing(duplicates, 15);

            user.setDuplicates(duplicates);
            user.setMissing(missing);

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

    private void deleteSelected() {
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Niste prijavljeni.");
            return;
        }

        for (int i = 1; i <= 99; i++) {
            if (duplicateBoxes.get(i).isSelected()) {
                user.getDuplicates().remove(i);
            }
            if (missingBoxes.get(i).isSelected()) {
                user.getMissing().remove(i);
            }
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
        detailsArea.setText(
                "Mozes da menjas slicice sa korisnikom " + info.getOtherUser()
                        + "\n\nTi imas za njega: " + info.getIGive()
                        + "\n\nOn za tebe ima: " + info.getHeGives()
        );
    }

    private void sendExchangeRequest() {
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Niste prijavljeni.");
            return;
        }

        try {
            ExchangeInfo info = (ExchangeInfo) exchangeCombo.getSelectedItem();
            if (info == null) {
                JOptionPane.showMessageDialog(this, "Odaberite korisnika za razmenu.");
                return;
            }

            List<Integer> mine = new ArrayList<>(info.getIGive());
            List<Integer> his  = new ArrayList<>(info.getHeGives());

            int count = Math.min(mine.size(), his.size());

            List<Integer> selectedMine;
            if (mine.size() > count) {
                StickerSelectionDialog dialog = new StickerSelectionDialog(this, mine, count);
                if (!dialog.isConfirmed()) {
                    return;
                }
                selectedMine = dialog.getSelected();
            } else {
                selectedMine = new ArrayList<>(mine);
            }

            List<Integer> selectedHis = new ArrayList<>(his.subList(0, count));

            ExchangeRequest request = new ExchangeRequest(
                    user.getUsername(),
                    info.getOtherUser(),
                    selectedMine,
                    selectedHis
            );

            out.reset();
            out.writeObject(request);
            out.flush();

            JOptionPane.showMessageDialog(this, "Zahtev poslat korisniku " + info.getOtherUser() + ".");

        } catch (Exception e) {
            e.printStackTrace();
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
                                for (Object o : list) {
                                    exchangeCombo.addItem((ExchangeInfo) o);
                                }
                            });
                        }
                    }

                    else if (obj instanceof ExchangeRequest) {
                        ExchangeRequest req = (ExchangeRequest) obj;

                        if (req.isAccepted()) {
                            SwingUtilities.invokeLater(() -> {
                                for (Integer num : req.getFromUserStickers()) {
                                    user.getDuplicates().remove(num);
                                }
                                for (Integer num : req.getToUserStickers()) {
                                    user.getMissing().remove(num);
                                }
                                refreshCheckBoxStates();

                                try {
                                    out.reset();
                                    out.writeObject(user);
                                    out.flush();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                                exchangeCombo.removeAllItems();
                                detailsArea.setText("");
                                JOptionPane.showMessageDialog(ClientFrame.this, "Razmena sa korisnikom " + req.getToUser() + " je prihvaćena i završena!");
                            });

                        } else {
                            SwingUtilities.invokeLater(() -> {
                                int answer = JOptionPane.showConfirmDialog(
                                        this,
                                        req.getFromUser() + " zeli razmenu.\n\n"
                                                + "Daje ti: " + req.getFromUserStickers()
                                                + "\n\nTrazi od tebe: " + req.getToUserStickers()
                                                + "\n\nPrihvatas?",
                                        "Zahtev za razmenu",
                                        JOptionPane.YES_NO_OPTION
                                );

                                if (answer == JOptionPane.YES_OPTION) {
                                    for (Integer num : req.getToUserStickers()) {
                                        user.getDuplicates().remove(num);
                                    }
                                    for (Integer num : req.getFromUserStickers()) {
                                        user.getMissing().remove(num);
                                    }
                                    refreshCheckBoxStates();

                                    try {
                                        out.reset();
                                        out.writeObject(user);
                                        out.flush();

                                        ExchangeRequest acceptance = new ExchangeRequest(
                                                req.getFromUser(),
                                                req.getToUser(),
                                                req.getFromUserStickers(),
                                                req.getToUserStickers(),
                                                true
                                        );
                                        out.reset();
                                        out.writeObject(acceptance);
                                        out.flush();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }

                                    exchangeCombo.removeAllItems();
                                    detailsArea.setText("");
                                    JOptionPane.showMessageDialog(ClientFrame.this, "Razmena uspesna!");
                                }
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
}