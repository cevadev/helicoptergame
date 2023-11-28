package com.ceva;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
public class Misc {
    public static void makeSameSize(JComponent... components) {
        int width = 0;
        int height = 0;
        for (JComponent c : components) {
            Dimension d = c.getPreferredSize();
            width = Math.max(width, d.width);
            height = Math.max(height, d.height);
        }
        for (JComponent c : components) {
            Dimension d = new Dimension(width, height);
            c.setPreferredSize(d);
            c.setMaximumSize(d);
        }
    }

    public static JMenuItem createMenuItem(String title, int keyCode, int modifiers, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
        if (actionListener != null)
            menuItem.addActionListener(actionListener);
        return menuItem;
    }

    public static JMenuItem createMenuItem(String title, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(title);
        if (actionListener != null)
            menuItem.addActionListener(actionListener);
        return menuItem;
    }
}
