package com.ceva;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
public class FoeHeli extends Sprite{
    HeliGame game;
    int y0;
    float yspeed;
    float gravity;
    int health = 0;
    int counter = 0;
    boolean disabledFire;
    int ai;             // Estrategia AI
    // campos static para evitar leer la info del disco cada vez que se genera un objeto
    private static BufferedImage[] images;
    private static BufferedImage[] explosion;

    private static int yWaveCounter = 0;

    static final int AI_COUNT    = 3;
    /*Comportamientos del enemigo*/
    // el enemigo se mueve en una direccion fija
    static final int AI_STRAIGHT = 0;
    // el enemigo se mueve horizontalmente, luego calcula si el juegador se mueve arriba o abajo
    // y dependiendo de ellos incrementa o decrementa la coordenada y
    static final int AI_YFOLLOW  = 1;
    // hace que el enemigo se mueva hacia arriba y hacia abajo alternadamente
    static final int AI_YWAVE    = 2;

    public FoeHeli(HeliGame mainPanel, int x, int y, int ai, int health, int state, int dx, boolean disabledFire) {
        this.game = mainPanel;
        width = 64;
        height = width;
        this.disabledFire = disabledFire;
        // bounds-> area que representa el helicopter
        bounds = new Rectangle(0, 22, 64, 20);
        // iniciamos la secuencia de imagenes
        if (images == null)
            // secuencia 1: helicoptero en movimiento
            images = Util.loadFrameSequence("foe-heli.png", width);
        if (explosion == null)
            // secuencia 2: helicoptero explosion
            explosion = Util.loadFrameSequence("explosion.png", width);

        // metodo del la clase Sprite: hacemos que el sprite soporte 2 secuencias de animaciones
        // que se mandan con initSequence()
        initAnimations(2);
        // animacion ciclica constante. la imagen cambiara cada 2 cuadros del juego
        initSequence(0, ANIMATION_LOOP, 2, images);
        // animacion sin repeticion con la velocidad de 6 para la explosion
        initSequence(1, ANIMATION_SINGLESEQUENCE, 6, explosion);

        // inicializacion de variables
        this.x = x;
        this.y = y;
        this.ai = ai;
        this.health = health;
        speedX = dx;
        counter = 0;
        y0 = y;
        if (ai == AI_YWAVE) {
            yspeed = (yWaveCounter++) % 2 == 0 ? 2 : -2; // (Util.rand_range(0, 1) > 0) ? 2 : -2;
            gravity = 0.05f;
        }
        setState(state);
    }

    public void hitDetected() {
        health--;
        if (health < 0)
            setState(Sprite.STATE_DYING);
        else {
            // Todavia no se muere, hacer blink
            blinkCount = 3;
        }
    }

    // metodo que implementa la Inteligencia Artificial del enemigo
    @Override
    public void nextFrame() {
        super.nextFrame();
        counter++;

        // validamos si el objeto esta activo, solo asi funciona la IA
        if (state == STATE_ACTIVE) {
// inicio de la inteligencia aritificial
            if (ai == AI_YFOLLOW) {
                // cambiamos la coordenada del objeto
                x += speedX;
                y += speedY;

                // Seguir a heli, pero me posiciono un poco mas abajo o un poco mas arriba
                // para que no me alcancen sus disparos.
                if ((y + bounds.y - 10) < game.heli.y)
                    y++;
                else if (y > (game.heli.y + game.heli.bounds.y - 6))
                    y--;
            } else if (ai == AI_YWAVE) {
                x += speedX;
                y += yspeed;

                if (y > y0) {
                    yspeed -= gravity;
                } else {
                    yspeed += gravity;
                }
            } else { // AI_STRAIGHT
                x += speedX;
                y += speedY;
            }
// fin de la inteligencia artificial
            if ((x + width) <= 0) {
                state = STATE_INACTIVE;
            } else if (((counter % 60) == 0) && !disabledFire) {
                // Disparar
                game.fireBullet(x, y + bounds.y + bounds.height/2, -4, 0, Bullet.BULLET_FOE);
            }
        } else if (state == STATE_DYING) {
            if ((curSequence == 1) && endOfSequence) {
                // Termino de explotar
                state = STATE_INACTIVE;
            }
        }
    }

}
