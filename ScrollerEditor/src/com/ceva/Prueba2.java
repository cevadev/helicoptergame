package com.ceva;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class Prueba2 extends JFrame {

    public Prueba2(){
        super();
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.BLACK);
        JPanel rightPanel = new JPanel();
        JPanel statusPanel = new JPanel();

        add(mainPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{
            Prueba2 frame = new Prueba2();
            frame.setVisible(true);
        });
    }
}
