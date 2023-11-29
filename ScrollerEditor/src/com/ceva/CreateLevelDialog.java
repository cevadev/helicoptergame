package com.ceva;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
public class CreateLevelDialog extends JDialog {
    private boolean cancelled = true;
    private JTextField txtNSeconds;

    public CreateLevelDialog(JFrame owner) {
        super(owner, "Create level");
        initComponents();
    }

    private void initComponents() {
        setModal(true);

        JPanel contentPane = (JPanel)getContentPane();
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        JPanel okCancelPanel = new JPanel();
        okCancelPanel.setLayout(new BoxLayout(okCancelPanel, BoxLayout.X_AXIS));

        JLabel lbl = new JLabel("Length (in seconds):");
        contentPanel.add(lbl);
        contentPanel.add(Box.createHorizontalStrut(10));
        txtNSeconds = new JTextField();
        txtNSeconds.setColumns(6);
        contentPanel.add(txtNSeconds);

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        okButton.addActionListener((e) -> {
            if (!validData())
                return;
            cancelled = false;
            setVisible(false);
        });
        cancelButton.addActionListener((e) -> {
            cancelled = true;
            setVisible(false);
        });
        Misc.makeSameSize(okButton, cancelButton);
        okCancelPanel.add(okButton);
        okCancelPanel.add(Box.createHorizontalStrut(6));
        okCancelPanel.add(cancelButton);

        contentPane.add(contentPanel);
        contentPane.add(Box.createVerticalStrut(10));
        contentPane.add(okCancelPanel);

        getRootPane().setDefaultButton(okButton);

        setResizable(false);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getOwner());
    }

    private boolean validData() {
        if (txtNSeconds.getText().length() == 0) {
            JOptionPane.showMessageDialog(this, "Selecciona el número de segundos que tendrá el nuevo nivel.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            Integer.parseInt(txtNSeconds.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Selecciona el número de segundos que tendrá el nuevo nivel.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean wasCancelled() {
        return cancelled;
    }

    public int getNSeconds() {
        try {
            return Integer.parseInt(txtNSeconds.getText());
        } catch (NumberFormatException e) {
            /* Esto nunca pasa porque ya fue validada la entrada, pero por si acaso,
            muestro el mensaje de error en la consola.
            */
            System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}

