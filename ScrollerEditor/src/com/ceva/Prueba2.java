package com.ceva;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class Prueba2 extends JFrame {
    private final Dimension preferredSize = new Dimension(640, 480);
    public Prueba2(){
        super();
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setPreferredSize(preferredSize);

        // hacemos que el right panel se divida en 2 para contener al UP y Down right panel
        JPanel rightPanel = new JPanel(new GridLayout(2, 1));
        JPanel rightUpPanel = new JPanel();
        JPanel rightDnPanel = new JPanel();
        // BoxLayout para color los elemento en vertical
        BoxLayout bl = new BoxLayout(rightUpPanel, BoxLayout.Y_AXIS);
        rightUpPanel.setLayout(bl);
        rightPanel.add(rightUpPanel);
        rightPanel.add(rightDnPanel);

        JPanel statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(mainPanel.getPreferredSize().width, 50));

        //add(mainPanel, BorderLayout.CENTER);


        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(mainPanel);
        scrollPane.setPreferredSize(preferredSize);

        JButton btnFree = new JButton("Free");
        JButton btnLine = new JButton("Line");
        JButton btnHeli = new JButton("Heli");

        // centramos los componentes
        btnFree.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        btnLine.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        btnHeli.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        // agrandamos el tamano del boton
        int minWidth = 72;
        Dimension ps = btnFree.getPreferredSize();
        if (ps.width < minWidth) {
            ps.width = minWidth;
            btnFree.setPreferredSize(ps);
        }
        // hacemos que los botones tengan todos el mismo tamano
        Misc.makeSameSize(btnFree, btnLine, btnHeli);

        // agregamos un espacio de 4px
        rightUpPanel.add(Box.createVerticalStrut(4));
        rightUpPanel.add(btnFree);
        rightUpPanel.add(Box.createVerticalStrut(4));
        rightUpPanel.add(btnLine);
        rightUpPanel.add(Box.createVerticalStrut(4));
        rightUpPanel.add(btnHeli);
        // agregamos un espacio horizontal
        rightUpPanel.add(Box.createHorizontalStrut(btnFree.getPreferredSize().width + 8));

        rightDnPanel.setLayout(new BoxLayout(rightDnPanel, BoxLayout.Y_AXIS));
        JRadioButton chkUp = new JRadioButton("Up");
        JRadioButton chkDn = new JRadioButton("Down");
        chkUp.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        chkDn.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        Misc.makeSameSize(chkUp, chkDn);
        ButtonGroup upDownGroup = new ButtonGroup();
        upDownGroup.add(chkUp);
        upDownGroup.add(chkDn);
        chkUp.setSelected(true);
        rightDnPanel.add(chkUp);
        rightDnPanel.add(chkDn);
        rightDnPanel.add(Box.createHorizontalStrut(chkUp.getPreferredSize().width + 8));

        add(rightPanel, BorderLayout.LINE_END);
        add(statusPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
        // aplicamos un cast al Container general
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initMenu();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(Misc.createMenuItem("Create new level...", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, null));
        fileMenu.add(Misc.createMenuItem("Load...",             KeyEvent.VK_F3, 0, null));
        fileMenu.add(Misc.createMenuItem("Save",                KeyEvent.VK_F2, 0, null));
        fileMenu.add(Misc.createMenuItem("Save As...",          null));
        fileMenu.add(new JPopupMenu.Separator());
        fileMenu.add(Misc.createMenuItem("Quit",                KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK, null));

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    public static void main(String[] args) {
        try{
            javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch (Exception ex){
            System.out.println(ex.getClass().getName() + " generated: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(()->{
            Prueba2 frame = new Prueba2();
            frame.setVisible(true);
        });
    }
}
