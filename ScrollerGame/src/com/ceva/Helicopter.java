package com.ceva;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Helicopter extends Sprite{
    private int pauseFramesLeft;
    private Screen screen;
    private KeyBoard keyboard;
    // donde cada constante representa un bit izq, derecha, arriba, abajo
    private static int JLEFT  = 1;
    private static int JRIGHT = 2;
    private static int JUP    = 4;
    private static int JDOWN  = 8;
    // variable que guarda el estabado de todas las direcciones
    private int joystickState = 0;

    public Helicopter(Screen screen, KeyBoard keyboard, int x, int y) {
        this.x = x;
        this.y = y;
        initAnimations(2);

        // rectangulo para las colisiones
        bounds = new Rectangle(0, 20, 64, 26);
        width = 64;
        height = 64;
        // 1era secuencia de imagenes
        BufferedImage normal[] = Util.loadFrameSequence("heli.png", 64);
        // 2da secuencia de imagenes
        BufferedImage crash[] = Util.loadFrameSequence("heli-crash.png", 64);
        // secuencia 0 sera una animacion continua que cambia cada 2 cuadros
        initSequence(0, ANIMATION_LOOP, 2, normal);
        initSequence(1, ANIMATION_SINGLESEQUENCE, 8, crash);

        this.screen = screen;
        this.keyboard = keyboard;
        initKeyboardListeners();  // inicializamos el teclado
        setState(STATE_ACTIVE); // state activo
    }

    // Listeners que actualizan joysticjState
    private void initKeyboardListeners() {
        keyboard.addKeyPressedListener(KeyEvent.VK_UP, (evt) -> {
            joystickState |= JUP;
            joystickState &= ~JDOWN;
        });
        keyboard.addKeyReleasedListener(KeyEvent.VK_UP, (evt) -> {
            joystickState &= ~JUP;
            if (keyboard.keyPressed(KeyEvent.VK_DOWN))
                joystickState |= JDOWN;
        });
        keyboard.addKeyPressedListener(KeyEvent.VK_DOWN, (evt) -> {
            joystickState |= JDOWN;
            joystickState &= ~JUP;
        });
        keyboard.addKeyReleasedListener(KeyEvent.VK_DOWN, (evt) -> {
            joystickState &= ~JDOWN;
            if (keyboard.keyPressed(KeyEvent.VK_UP))
                joystickState |= JUP;
        });

        // presionamos tecla izquierda
        keyboard.addKeyPressedListener(KeyEvent.VK_LEFT, (evt) -> {
            // si presiona tecla izq, se activa el bit JLEFT
            joystickState |= JLEFT;
            // Desactivamos el bit JRIGHT
            joystickState &= ~JRIGHT;
        });
        // liberamos tecla izquierda
        keyboard.addKeyReleasedListener(KeyEvent.VK_LEFT, (evt) -> {
            // Desactivamos JLEFT
            joystickState &= ~JLEFT;
            // Validamos si aun sigue presionada la tecla derecha
            if (keyboard.keyPressed(KeyEvent.VK_RIGHT))
                // activamos el bit JRIGHT
                joystickState |= JRIGHT;
        });
        keyboard.addKeyPressedListener(KeyEvent.VK_RIGHT, (evt) -> {
            joystickState |= JRIGHT;
            joystickState &= ~JLEFT;
        });
        keyboard.addKeyReleasedListener(KeyEvent.VK_RIGHT, (evt) -> {
            joystickState &= ~JRIGHT;
            if (keyboard.keyPressed(KeyEvent.VK_LEFT))
                joystickState |= JLEFT;
        });
    }

    @Override
    void setState(int state) {
        super.setState(state);
        if (state == STATE_DYING)
            // despues de muerto esperamos 3 segundo
            pauseFramesLeft = 60*3;
    }

    @Override
    public void hitDetected() {
        setState(STATE_DYING);
    }

    /**
     * metodo que es invocado desde la clase principal. Play indica lo que va a suceder si
     * el helicóptero esta activo o inactivo
     * @param game
     */
    public void play(HeliGame game) {
        if (state == Sprite.STATE_ACTIVE) {
            int screenWidth = screen.getWidth();
            int screenHeight = screen.getHeight();

            // manejo del teclado. validamos si se movio a la derecha
            if ((joystickState & JRIGHT) != 0) {
                // nos movemos a la derecha
                x += 2;
                // validamos q no se haya llegado al final de la pantalla
                if ((x+bounds.width) > screenWidth)
                    // si se paso, entonces lo regresamos
                    x = screenWidth - bounds.width;
            }
            // validamos si se movio a la izquierda
            else if ((joystickState & JLEFT) != 0) {
                // movemos a la izq
                x -= 2;
                // validamos q no se pase de la pantalla
                if (x < 0)
                    x = 0;
            }

            // validamos si se movio arriba
            if ((joystickState & JUP) != 0) {
                // movemos arriba
                y -= 2;
                if (y <= -bounds.y)
                    y = -bounds.y;
            }
            // validamos si se movio abajo
            else if ((joystickState & JDOWN) != 0) {
                // movemos abajo
                // y es la orilla de la imagen, bounds.y es donde empieza a dibujarse el helicopter
                // bounds.height es el alto de la imagen del helicoptero
                y += 2;
                if ((y+bounds.y + bounds.height) > screenHeight)
                    y = screenHeight - bounds.y - bounds.height;
            }

            // validamos si presiona la tecla control (tecla disparo)
            if (keyboard.keyPressed(KeyEvent.VK_CONTROL)) {
                // disparo desde el frente del helicopter
                game.fireBullet(x+width, y+height/2, 8, 0, 0);
                // ya no recibimos notificacion que control ha sido presionado hasta que se libere la key
                keyboard.ignoreKey(KeyEvent.VK_CONTROL);
            }

            /* *** Colision con escenario *** */
            // obtenemos el nivel
            ScrollLevel scrollLevel = game.scrollLevel;
            // x -> coordenada del helicoptero
            int xLvl = (scrollLevel.curXLevel + x + 4) % scrollLevel.levelL.length;
            for (int n=0; n<bounds.width-8; n++) {
                // Colision por abajo
                if (scrollLevel.levelL[xLvl] <= (y + bounds.y + bounds.height-4)) {
                    hitDetected();
                    break;
                }
                // Colision por arriba
                if ((scrollLevel.levelH[xLvl]) >= (y + bounds.y+4)) {
                    hitDetected();
                    break;
                }
                // no hay colision
                xLvl = (xLvl + 1) % scrollLevel.levelL.length;
            }
        }
        // estado no activo
        else if (pauseFramesLeft > 0) {
            x--;
            pauseFramesLeft--;
            if (pauseFramesLeft <= 0) {
                game.restartLevel();
            }
        }
        // sprite
        nextFrame();
    }


}
