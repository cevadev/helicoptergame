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

    /*
     * Metodo que realiza las tareas de inicializacion del juego y
     * creacion de todos los objetos que participan en el juego.
     */
    private void init(){
        setBackground(Color.BLUE);
        Util.defaultClassPath = "/com/ceva/";
        fontTitle = screen.getGraphics2D().getFont().deriveFont(24f);// tipo letra para titulos

        startTime = System.currentTimeMillis();// save hora actual
        keyboard = new KeyBoard(this);// creamos keyboar
        // inicializamos el helicoptero
        heli = new Helicopter(screen, keyboard, 100, screen.getHeight()/2);

        bullets = new Bullet[128];// cantidad de balas que aparecera al mismo tiempo
        for (int n=0; n<bullets.length; n++)
            bullets[n] = new Bullet(); // creamos de una vez todas las balas
        // comenzamos el nivel
        scrollLevel = ScrollLevel.createPlainLevel(screen, screen.getWidth()*3/2+30, 1);
        // listener que se activa cuando presionamos scape, pasamos un callback
        keyboard.addKeyPressedListener(KeyEvent.VK_ESCAPE, (evt) -> {
            // cuando player presione escape doneFlag true y se temrina el juego
            doneFlag = true;
            if (onPause)
                resumeGame();
        });
        // listener que se activa cuando presiona ALT
        // evt.consume-> le indica a swing que consdere el evento como procesado y no lo envie
        keyboard.addKeyPressedListener(KeyEvent.VK_ALT, (evt) -> evt.consume());
    }

    private void startGame() {
        if (thread == null) {
            // creamos un nuevo thread para HeliGame
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start(); // este metodo llama al metodo run()
        }
    }

    private void resumeGame() {
        synchronized(this) {
            notify();
        }
    }

    /*
     * hacemos todas la limpieza necesaria, antes de terminar el juego
     */
    private void done() {
        keyboard.close();
        screen.done();

        JFrame jFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        jFrame.dispose();
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

    private void delay(int numFrames) {
        try {
            Thread.sleep((1000/Screen.FPS)*numFrames);
        } catch (InterruptedException e) {
        }
        startTime = System.currentTimeMillis();
    }

    /**
     *
     * @param x coordenada en x inicial
     * @param y coordenada en y inicial
     * @param dx desplazamiento en x
     * @param dy desplazamiento en y
     * @param state estado de la bala (bala enemigo o bala del jugador)
     */
    public void fireBullet(int x, int y, int dx, int dy, int state) {
        // recorremos el arreglo de balas
        for (Bullet b : bullets) {
            // preguntamos si la primera bala esta inactiva, la usamos para disparar
            if ((b.state & Bullet.BULLET_ACTIVE) == 0) {
                // habilitamos la bala
                b.state = state | Bullet.BULLET_ACTIVE;
                b.x = x;
                b.y = y;
                b.dx = dx;
                b.dy = dy;
                break;
            }
        }
    }

    /*
     * Manejo de la logica de las balas, animacion y verificar colision con objetos
     */
    private void doBullets() {
        // codigo para cada bala que se encuentra en el escenario
        bullet_for:
        for (Bullet b : bullets) {
            // si la bala esta desactivada o no activa entonces continue, es decir, saltamos a la sgte bala
            if ((b.state & Bullet.BULLET_ACTIVE) == 0)
                continue; // continuamos al sgte for

            // la bala esta activa
            // Colision con escenario (asumimos q la bala se mueve horizontalmente)
            // ciclo desde la posicion de la bala hasta el # de pixeles que se desplazara
            // curXLevel -> indice a partir de donde empieza el escenario
            int lvlX = (scrollLevel.curXLevel + b.x) % scrollLevel.levelL.length;
            int n = b.x; // obtenemos coordenada x de la bala
            /**
             * en el ciclo while: si hay colision con el escenario, se deshabilita la bala
             */
            while (n < b.x + b.dx) {
                // Colision con terreno
                // scrollLevel.levelL[lvlX] -> indica el punto a partir del cual hacia abajo es puro terreno
                // si la coordenada b.y es superior a scrollLevel.levelL[lvlX] significa que toco terreno
                // si la bala choca con el limite superior o inferior entonces hubo una colision
                if ((b.y >= scrollLevel.levelL[lvlX]) || (b.y <= scrollLevel.levelH[lvlX])) {
                    b.state &= ~Bullet.BULLET_ACTIVE; // como choco con el terreno, deshabilitamos la bala
                    // si solo colocamo continue, pasaria al sgte ciclo del while
                    // pero con la etiqueta bullet_for, hacemos un continue a nivel for
                    // entonces, deshabilitamos la bala actual y pasamos a la siguiente bala
                    continue bullet_for;
                }
                // si no hay colision en este punto, revisamos el siguiente.
                lvlX = (lvlX + 1) % scrollLevel.levelL.length;
                n++;
            }
            // la bala aun sigue activa
            // hacemos la animacion
            b.x += b.dx;
            b.y += b.dy;

            // Ya salio de pantalla?
            // b.x>screen.getWidth() -> la bala va hacia la derecha que es la bala del player
            // b.x<0 -> la bala hacia la izq que es la bala enemiga
            if ((b.x > screen.getWidth()) || (b.x < 0)) {
                b.state &= ~Bullet.BULLET_ACTIVE; // desactivamos la bala
            } else {
                // si no se salio de la pantalla entonces verifico si hay colision
                // si Bullet.BULLET_FOE == 0, no es bala enemiga sino del jugador
                if ((b.state & Bullet.BULLET_FOE) == 0) {
                    // Bala del jugador. entonces verificamos si hay colision contra los enemigos
                    // recorremos a los enemigos
                    for (Sprite s : foes) {
                        // validamos si el sprite esta inactivos, en cuyo caso pasamos al sgte jugador
                        if (s.state != Sprite.STATE_ACTIVE)
                            continue;
                        /* Verificamos la colision contra el enemigo
                         * Para que exista una colision la coordenada x de la bala (b.x) tiene que ser mayor
                         * al limite de la izquiera(s.x+s.bounds.x) o
                         * menor al limite de la derecha (s.x+s.bounds.x+s.bounds.width) y
                         * los mismo para la coordena y de la bala. Si todo se cumple,
                         * la bala le dio al enemigo.
                         */
                        if ((b.x >= (s.x + s.bounds.x)) && (b.x <= (s.x + s.bounds.x + s.bounds.width)) &&
                                (b.y >= (s.y + s.bounds.y)) && (b.y <= (s.y + s.bounds.y + s.bounds.height))
                        ) {
                            // si la bala le dio al enemigo, desactivamos la bala
                            b.state &= ~Bullet.BULLET_ACTIVE;
                            s.hitDetected(); // llamamos al Sprite.hitDetected()
                        }
                    }
                }
                else if (heli.state == Sprite.STATE_ACTIVE) {
                    // Es una bala de enemigo, entonces verificamos colision contra el jugador
                    if ((b.x >= (heli.x + heli.bounds.x)) && (b.x <= (heli.x + heli.bounds.x + heli.bounds.width)) &&
                            (b.y >= (heli.y + heli.bounds.y)) && (b.y <= (heli.y + heli.bounds.y + heli.bounds.height))
                    ) {
                        // si la bala le dio al jugador, entonces
                        b.state &= ~Bullet.BULLET_ACTIVE; // deshabilitamos la bala
                        heli.hitDetected();
                    }
                }
            }
        }
    }

    public void restartLevel() {
        // Desactivar enemigos
        for (Sprite s : foes) {
            if (s != null)
                s.state = Sprite.STATE_INACTIVE;
        }
        // Desactivar balas
        for (Bullet b : bullets) {
            if (b != null)
                b.state = 0;
        }

        // Siguiente vida
        scrollLevel.curXLevel = 0;
        scrollLevel.curFoePtr = scrollLevel.foeList;
        if (scrollLevel.next != null) {
            scrollLevel.next.curFoePtr = scrollLevel.next.foeList;
        }

        // Insertar un espacio en blanco para reiniciar el nivel.
        if (scrollLevel.levelType != ScrollLevel.LEVELTYPE_FLAT) {
            ScrollLevel lvl = ScrollLevel.createPlainLevel(screen, screen.getWidth()*3/2+30, scrollLevel.numLevel);
            lvl.next = scrollLevel;
            scrollLevel = lvl;
        }

        heli.setState(Sprite.STATE_ACTIVE);
        heli.x = 10;
        heli.y = (screen.getHeight()/2) + heli.bounds.y - heli.bounds.height/2;
    }

    private void addNewFoes(ScrollLevel lvl, int offset) {
        int screenWidth = screen.getWidth();
        while ((lvl.curFoePtr != null) && (lvl.curFoePtr.x+offset <= (scrollLevel.curXLevel+screenWidth))) {
            System.out.printf("Create enemy. curXLevel:%d, curXLevel+width:%d, curFoePtr.x=%d, curFoePtr.y=%d\n",
                    scrollLevel.curXLevel, scrollLevel.curXLevel+screenWidth, lvl.curFoePtr.x, lvl.curFoePtr.y);
            // Create new enemy
            FoeHeli fh = new FoeHeli(this, lvl.curFoePtr.x + offset - scrollLevel.curXLevel, lvl.curFoePtr.y,
                    lvl.curFoePtr.ai, lvl.curFoePtr.health, Sprite.STATE_ACTIVE, -2,
                    lvl.curFoePtr.disabledFire);
            foes.add(fh);

            lvl.curFoePtr = lvl.curFoePtr.next;
            if ((lvl.curFoePtr == null) || (lvl.curFoePtr.x+offset > (scrollLevel.curXLevel+screenWidth)))
                break;
        }
    }

    /*
     * metodo que controla el manejo de los enemigos.
     * se agregan nuevos enemigos conforme se avanza en el nivel y verificacion de colision
     * con el jugador
     */
    private void doEnemies() {
        // agregamos los enemigos que aparecen
        addNewFoes(scrollLevel, 0);
        if ((scrollLevel.curXLevel+screen.getWidth() > scrollLevel.width) && (scrollLevel.next != null)) {
            addNewFoes(scrollLevel.next, scrollLevel.width);
        }

        if (heli.state == Sprite.STATE_ACTIVE) {
            Rectangle hBounds = new Rectangle(heli.x + heli.bounds.x, heli.y + heli.bounds.y, heli.bounds.width, heli.bounds.height);
            for (Iterator<Sprite> it = foes.iterator(); it.hasNext(); ) {
                Sprite s = it.next();
                s.nextFrame();
                if (s.state == Sprite.STATE_INACTIVE) {
                    it.remove();
                    continue;
                } else if (s.state == Sprite.STATE_ACTIVE) {
                    // Checar colision con jugador
                    Rectangle sBounds = new Rectangle(s.x + s.bounds.x, s.y + s.bounds.y, s.bounds.width, s.bounds.height);
                    if (hBounds.intersects(sBounds)) {
                        heli.hitDetected();
                        if (heli.state != Sprite.STATE_ACTIVE)
                            continue;
                    }
                }
            }
        } else {
            for (Sprite s : foes)
                s.nextFrame();
        }
    }

    /*
     * metodos que se utiliza una vez antes de comenzar el juego
     */
    private void presentation() {
        int incX = 2; // incremento en x
        int screenWidth = screen.getWidth(); // ancho de la pantalla
        int screenHeight = screen.getHeight(); // alto de la pantalla
        // 2do ciclo principal
        while (!doneFlag) {
            // animamos el helicoptero y mostramos el mensaje PRESS <ENTER> TO START
            heli.x += incX; // movemos el helicoptero
            // validamos si llegamos al final de la pantalla
            if (heli.x+heli.width >= screenWidth) {
                heli.x = screenWidth - heli.width;
                incX = -incX;// movemos el helicoptero en sentido contrario
            }
            else if (heli.x <= 0) {
                heli.x = 0; // cambiamos la direccion del helicoptero
                incX = -incX;
            }

            // si presionamos enter
            if (keyboard.keyPressed(KeyEvent.VK_ENTER)) {
                keyboard.ignoreKey(KeyEvent.VK_ENTER);
                break;
            }
            // animacion del sprite del helicoptero
            heli.nextFrame();
            // dibujo del escenario
            draw();
            Graphics2D g2d = screen.getGraphics2D(); // dibujo del titulo
            g2d.setColor(Color.WHITE);
            g2d.setFont(fontTitle);
            FontMetrics fm = g2d.getFontMetrics();
            String str = "Presione <ENTER> para iniciar";
            // centrado del mensaje
            g2d.drawString(str, screenWidth/2 - fm.stringWidth(str)/2, screenHeight/2 - fm.getHeight()/2 );
            repaintAndWait();
        }

        if (!doneFlag)
            delay(60);
        heli.x = 50;
    }

    /*
     * Dibujo de la pantalla principal y del juego
     */
    private void draw() {
        Graphics2D g2d = screen.getGraphics2D();
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, screen.getWidth(), screen.getHeight());

        // escenario
        scrollLevel.draw(g2d);

        // bullets
        for (Bullet b : bullets)
            b.draw(g2d);

        // helicopter
        heli.draw(g2d);

        // Enemies
        for (Sprite s : foes)
            s.draw(g2d);
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
            // get referencia al sgte nivel
            scrollLevel = scrollLevel.scroll(1); // movemos escenario 1 pixel a la izq
            doBullets();
            heli.play(this); // turno del jugador
            doEnemies();

            draw();
            repaintAndWait();
        }
        // el juego finaliza cuando doneFlag es true
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
