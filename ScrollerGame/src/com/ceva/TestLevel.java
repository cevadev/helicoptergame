package com.ceva;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author rcosio
 */
public class TestLevel extends JPanel {
    private final Dimension size = new Dimension(640, 480);
    private javax.swing.Timer timer;
    private ScrollLevel scrollLevel;
    private Screen screen;

    public TestLevel() {
        super();
        setBackground(Color.BLUE);

        Util.defaultClassPath = "/com/ceva/";

        screen = new Screen(size.width, size.height);
        scrollLevel = ScrollLevel.createPlainLevel(screen, size.width*3/2, 1);

        timer = new Timer(1000/30, (e) -> {
            scrollLevel = scrollLevel.scroll(1);
            repaint();
        });
        timer.start();
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        scrollLevel.draw((Graphics2D)g);
    }

    private void stopTimer() {
        timer.stop();
    }

    public static void main(String args[]) throws Exception {
        SwingUtilities.invokeLater(() -> {
            TestLevel panel = new TestLevel();
            JFrame frame = new JFrame("Helicopter");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(panel);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    panel.stopTimer();
                }
            });
            frame.pack();
            frame.setLocationRelativeTo(null);

            frame.setVisible(true);
        });
    }
}