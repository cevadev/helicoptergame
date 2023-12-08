package com.ceva;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class MainFrame extends JFrame {
    private LevelView mainPanel;
    private StatusBarView statusBar;
    private final Dimension preferredSize = new Dimension(640, 480);
    public MainFrame(){
        super("Level Editor");
        initComponents();
    }

    private void initComponents() {
        // hacemos que el right panel se divida en 2 para contener al UP y Down right panel
        JPanel rightPanel = new JPanel(new GridLayout(2, 1));
        JPanel rightUpPanel = new JPanel();
        JPanel rightDnPanel = new JPanel();
        // BoxLayout para color los elemento en vertical
        BoxLayout bl = new BoxLayout(rightUpPanel, BoxLayout.Y_AXIS);
        rightUpPanel.setLayout(bl);
        rightPanel.add(rightUpPanel);
        rightPanel.add(rightDnPanel);

        mainPanel = new LevelView();

        //JPanel statusPanel = new JPanel();
        //statusPanel.setPreferredSize(new Dimension(mainPanel.getPreferredSize().width, 50));

        statusBar = new StatusBarView();
        statusBar.setLevelView(mainPanel);
        statusBar.setPreferredSize(new Dimension(preferredSize.width, 50));
        //add(mainPanel, BorderLayout.CENTER);

        mainPanel.setStatusBar(statusBar);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(mainPanel);
        scrollPane.setPreferredSize(preferredSize);

        JButton btnFree = new JButton("Free");
        JButton btnLine = new JButton("Line");
        JButton btnHeli = new JButton("Heli");
        // al hacer click en algun boton llamamos al metodo setCurrentTool() del controller
        // el user selecciona boton free
        btnFree.addActionListener((e) -> mainPanel.getController().setCurrentTool(LevelEditorModel.TOOL_FREE));
        // el user selecciona el boton line
        btnLine.addActionListener((e) -> mainPanel.getController().setCurrentTool(LevelEditorModel.TOOL_LINE));
        // el user selecciona el boton helicopter
        btnHeli.addActionListener((e) -> mainPanel.getController().setCurrentTool(LevelEditorModel.TOOL_HELI));

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
        chkUp.addActionListener((e) -> mainPanel.getController().setUpOrDown(chkUp.isSelected()));
        chkDn.addActionListener((e) -> mainPanel.getController().setUpOrDown(chkUp.isSelected()));
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

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        add(scrollPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.LINE_END);
        // aplicamos un cast al Container general
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initMenu();
        addWindowListener(new WindowAdapter() {
            // si el user cierra la ventana preguntamos si quiere guardar los cambios
            @Override
            public void windowClosing(WindowEvent e) {
                onQuit();
            }
        });

        //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private void onQuit() {
        if (mainPanel.getController().validateClose())
            dispose();
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(Misc.createMenuItem("Create new level...", KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, (e) -> mainPanel.getController().createNewLevel()));
        fileMenu.add(Misc.createMenuItem("Load...",             KeyEvent.VK_F3, 0, (e) -> mainPanel.getController().doLoad()));
        fileMenu.add(Misc.createMenuItem("Save",                KeyEvent.VK_F2, 0, (e) -> mainPanel.getController().doSave()));
        fileMenu.add(Misc.createMenuItem("Save As...",          (e) -> mainPanel.getController().doSaveAs()));
        fileMenu.add(new JPopupMenu.Separator());
        fileMenu.add(Misc.createMenuItem("Quit",                KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK, (e) -> onQuit()));

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }
    public LevelView getView() {
        return mainPanel;
    }

    public StatusBarView getStatusBar() {
        return statusBar;
    }

}
