package client;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StickerSelectionDialog extends JDialog {

    private List<Integer> selected;
    private boolean confirmed = false;

    public StickerSelectionDialog(JFrame parent, List<Integer> options, int neededCount) {
        super(parent, "Odaberi slicice", true);
        setSize(400, 400);
        setLayout(new BorderLayout());

        JPanel center = new JPanel(new GridLayout(0, 4));
        HashMap<Integer, JCheckBox> map = new HashMap<>();

        for (Integer num : options) {
            JCheckBox cb = new JCheckBox(String.valueOf(num));

            map.put(num, cb);
            center.add(cb);
        }

        JButton confirm = new JButton("Potvrdi");

        confirm.addActionListener(e -> {
            selected = new ArrayList<>();
            for (Integer num : map.keySet()) {
                if (map.get(num).isSelected()) {
                    selected.add(num);
                }
            }

            if (selected.size() != neededCount) {
                JOptionPane.showMessageDialog(this, "Moras odabrati tacno " + neededCount + " slicica.");
                return;
            }
            confirmed = true;
            dispose();
        });

        add(new JScrollPane(center), BorderLayout.CENTER);
        add(confirm, BorderLayout.SOUTH);

        setLocationRelativeTo(parent);
        setVisible(true);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public List<Integer> getSelected() {
        return selected;
    }
}