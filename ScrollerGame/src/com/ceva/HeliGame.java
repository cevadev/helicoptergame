package com.ceva;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
public class HeliGame extends JPanel implements Runnable{
    private final Dimension preferredSize = new Dimension(640, 480);
    private boolean doneFlag = false; // controlamos el fin de la animacion
    private Thread thread; // refrencia a thread para la animacion
    private Screen screen; // donde dibujamos
    private Font fontTitle;
    private long startTime; // nro cuadros por segundo en la animacion
    private KeyBoard keyboard; // referencia al teclado
    private boolean onPause; // hacer pausa

    // Variables del juego
    public Helicopter heli; // helicoptero
    private Bullet bullets[]; // balas
    public ScrollLevel scrollLevel; // escenario

    private List<Sprite> foes = new LinkedList<>(); // lista de enemigos

    public HeliGame() {
        super();
        screen = new Screen(preferredSize.width, preferredSize.height);
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    private void init(){}

    private void startGame() {
        if (thread == null) {
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void resumeGame() {
        synchronized(this) {
            notify();
        }
    }

    // animacion
    private void repaintAndWait() {
        synchronized(this) {
            repaint();
            try {
                wait(); // pausamos el thread de la animacion
            } catch (InterruptedException ex) {
            }
        }
        long curTime = System.currentTimeMillis();
        if ((curTime-startTime) < (1000/Screen.FPS)) {
            try {
                Thread.sleep((1000/Screen.FPS) - (curTime-startTime));
            } catch (InterruptedException e) {
            }
        }
        startTime = System.currentTimeMillis();
    }

    public void fireBullet(int x, int y, int dx, int dy, int state) {
        for (Bullet b : bullets) {
            if ((b.state & Bullet.BULLET_ACTIVE) == 0) {
                b.state = state | Bullet.BULLET_ACTIVE;
                b.x = x;
                b.y = y;
                b.dx = dx;
                b.dy = dy;
                break;
            }
        }
    }
    @Override
    public void run() {
        init();
        repaintAndWait();

        presentation();
        // Agrego pausa despues de haber terminado la presentacion
        IKeyCallback callback = (e) -> {
            onPause = !onPause;
            if (!onPause)
                resumeGame();
        };
        keyboard.addKeyPressedListener(KeyEvent.VK_PAUSE, callback);
        keyboard.addKeyPressedListener(KeyEvent.VK_P, callback);
        while (!doneFlag) {
            scrollLevel = scrollLevel.scroll(1);
            doBullets();
            heli.play(this);
            doEnemies();

            draw();
            repaintAndWait();
        }
        done();
    }

    @Override
    protected void paintComponent(Graphics g) {
        screen.paint((Graphics2D)g);
        if (!onPause) {
            synchronized(this) {
                // continua el thread de la animacion
                notify();
            }
        }
    }
    public static void main(String args[]) throws Exception {
        SwingUtilities.invokeLater(() -> {
            HeliGame panel = new HeliGame();
            JFrame frame = new JFrame("Helicopter - Raul Cosio");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setResizable(false);
            frame.addWindowListener(new WindowAdapter() {
                private boolean windowPaused;
                // se ejecuta el metodo cuando se abre la ventana
                @Override
                public void windowOpened(WindowEvent e) {
                    panel.startGame();
                }
                @Override
                public void windowActivated(WindowEvent e) {
                    /*
                    Nota: El diablo esta en los detalles, si el usuario presiona
                    pausa y cambia de ventana, cuando regrese deberia continuar
                    en pause.
                    */
                    if (!windowPaused)
                        return;

                    if (panel.onPause) {
                        panel.onPause = false;
                        panel.resumeGame();
                    }
                    windowPaused = false;
                }
                @Override
                public void windowDeactivated(WindowEvent e) {
                    if (!panel.onPause) {
                        panel.onPause = true;
                        windowPaused = true;
                    }
                }
            });
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocus(); // reconocer los eventos del teclado
        });
    }
}
