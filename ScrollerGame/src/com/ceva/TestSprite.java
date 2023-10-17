package com.ceva;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class TestSprite extends JPanel {
    private Sprite heli;
    private javax.swing.Timer timer;

    public TestSprite() {
        super();
        setBackground(Color.GRAY);

        com.ceva.Util.defaultClassPath = "/com/ceva/";

        heli = new Sprite();
        heli.initAnimations(1);
        BufferedImage heliFrames[] = com.ceva.Util.loadFrameSequence("heli.png", 64);
        heli.initSequence(0, Sprite.ANIMATION_LOOP, 2, heliFrames);
        heli.x = 150;
        heli.y = 100;
        heli.setState(Sprite.STATE_ACTIVE);

        timer = new Timer(1000/30, (e) -> {
            heli.nextFrame();
            repaint(); // llama a paintComponent()
        });
        timer.start();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 300);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        heli.draw((Graphics2D) g);
    }

    private void stopTimer() {
        timer.stop();
    }

    public static void main(String args[]) throws Exception {
        SwingUtilities.invokeLater(() -> {
            TestSprite panel = new TestSprite();
            JFrame frame = new JFrame("Helicopter");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(panel);
            // cuando se cierra la ventana, detenemos el timer
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
