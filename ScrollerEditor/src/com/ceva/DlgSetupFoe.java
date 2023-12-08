package com.ceva;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author rcosio
 * @date 13/agosto/2020 9:50 pm
 */
public class DlgSetupFoe extends JDialog {
    private boolean cancelled = true;
    JComboBox<String> cboFoeType;
    JComboBox<String> cboAI;
    JComboBox<String> cboStrength;
    JCheckBox chkDisabledFire;

    public DlgSetupFoe(JFrame owner) {
        super(owner, "Properties");
        initComponents();
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel();
        JPanel okCancelPanel = new JPanel();

        okCancelPanel.setLayout(new BoxLayout(okCancelPanel, BoxLayout.X_AXIS));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        okButton.addActionListener((e) -> {
            cancelled = false;
            setVisible(false);
        });
        cancelButton.addActionListener((e) -> {
            cancelled = true;
            setVisible(false);
        });
        Misc.makeSameSize(okButton, cancelButton);
        okCancelPanel.add(okButton);
        okCancelPanel.add(Box.createHorizontalStrut(5));
        okCancelPanel.add(cancelButton);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        add(contentPanel);
        add(okCancelPanel);

        JLabel lbl;
        chkDisabledFire = new JCheckBox("Disabled fire");
        cboFoeType = new JComboBox<>();
        cboFoeType.setModel(new DefaultComboBoxModel<>(new String[] {"HELI"}));

        cboAI = new JComboBox<>();
        cboAI.setModel(new DefaultComboBoxModel<>(new String[] { "STRAIGHT", "YFOLLOW", "YWAVE" }));

        cboStrength = new JComboBox<>();
        cboStrength.setModel(new DefaultComboBoxModel<>(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"}));

        GridBagLayout gl = new GridBagLayout();
        contentPanel.setLayout(gl);
        GridBagConstraints c = new GridBagConstraints();

        lbl = new JLabel("Foe type:");
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(4, 10, 4, 10);
        c.anchor = GridBagConstraints.LINE_START;
        contentPanel.add(lbl, c);

        c.gridx = 1;
        c.gridy = 0;
        contentPanel.add(cboFoeType, c);

        lbl = new JLabel("AI:");
        c.gridx = 0;
        c.gridy = 1;
        contentPanel.add(lbl, c);

        c.gridx = 1;
        c.gridy = 1;
        contentPanel.add(cboAI, c);

        lbl = new JLabel("Strength:");
        c.gridx = 0;
        c.gridy = 2;
        contentPanel.add(lbl, c);

        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.CENTER;
        contentPanel.add(cboStrength, c);

        c.gridx = 1;
        c.gridy = 3;
        c.insets = new Insets(0, 0, 5, 0);
        contentPanel.add(chkDisabledFire, c);

        if (cboFoeType.getPreferredSize().width < 96) {
            Dimension d = cboFoeType.getPreferredSize();
            cboFoeType.setPreferredSize(new Dimension(96, d.height));
        }
        Misc.makeSameSize(cboFoeType, cboAI, cboStrength);

        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        getRootPane().setDefaultButton(okButton);

        setResizable(false);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getOwner());
        setModal(true);
    }

    public boolean wasCancelled() {
        return cancelled;
    }

    public void setAI(int ai) {
        // inicializamos los controles del dialog con los datos del objeto enemigo
        if (ai == Foe.FOE_AI_STRAIGHT) {
            cboAI.setSelectedItem("STRAIGHT");
        } else if (ai == Foe.FOE_AI_YFOLLOW) {
            cboAI.setSelectedItem("YFOLLOW");
        } else if (ai == Foe.FOE_AI_YWAVE) {
            cboAI.setSelectedItem("YWAVE");
        }
    }

    public void setStrength(int strength) {
        cboStrength.setSelectedIndex(strength);
    }

    public void setDisabledFire(boolean disabledFire) {
        chkDisabledFire.setSelected(disabledFire);
    }

    public int getAI() {
        String s = (String) cboAI.getSelectedItem();
        int ai = 0;
        if ("STRAIGHT".equals(s))
            ai = Foe.FOE_AI_STRAIGHT;
        else if ("YFOLLOW".equals(s))
            ai = Foe.FOE_AI_YFOLLOW;
        else if ("YWAVE".equals(s))
            ai = Foe.FOE_AI_YWAVE;

        return ai;
    }

    public int getStrength() {
        return (1 << cboStrength.getSelectedIndex()) - 1;
    }

    public boolean getDisabledFire() {
        return chkDisabledFire.isSelected();
    }

}
