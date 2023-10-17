package com.ceva;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Clase que realiza la animacion de imagenes
 */
public class Sprite {
    int state;      // Estado del sprite (STATE_ACTIVE, STATE_DYING, ...)
    int x;          // Coordenada X donde se dibuja sprite.
    int y;          // Coordenada Y donde se dibuja sprite.
    int width;      // Ancho del sprite.
    int height;     // Alto del sprite.
    int speedX;
    int speedY;
    Rectangle bounds;   // Dimension del objeto (usado para colision).
    int blinkCount = 0; // Para que parpadee cuando lo toca una bala pero aun no muere.

    /* Estados del sprite. Cada estado esta relacionado a una secuencia de animacion. */
    static final int STATE_INACTIVE = 0; // Desactivado
    static final int STATE_ACTIVE   = 1; // Normal.
    static final int STATE_DYING    = 2; // Animacion para morirse (explosion normalmente).

    static final int ANIMATION_SINGLESEQUENCE = 0;  // Animar una sola vez.
    static final int ANIMATION_LOOP = 1;            // Animar constantemente.

    int frameCounter;     // Cuando llega a 0, cambia de frame.
    int curSequence;      // Sequencia de imagenes actual
    int curFrame;         // imagen actual en activeFrames a dibujar.

    /**
     * La secuencia de animaciones esta definida en frames que es un array bidimensional de tipo
     * BufferedImage. La primer dimension es el numero de secuencia y la segunda es la lista de
     * imagenes que forman la animacion.
     */
    BufferedImage frames[][];
    // referencia a la sencuencia activa de animacion, ejem: frames[curSequence]
    BufferedImage activeFrames[];
    // velocidad de la animacion
    int frameSpeed[];     // Numero de cuadros antes de cambiar de imagen.
    int animationMode[];  /* 0 = La animacion se queda fija en el ultimo cuadro.
                             1 = La animacion se repite despues del ultimo cuadro.
                          */
    boolean endOfSequence; // true cuando ya se acabo la animacion.

    /**
     * Inicializacion del sprite
     * @param sequences: numero de secuencias que manejara
     */
    void initAnimations(int sequences) {
        frames = new BufferedImage[sequences][];
        animationMode = new int[sequences];
        Arrays.fill(animationMode, ANIMATION_LOOP); // inicializamos el arreglo animationMode
        frameSpeed = new int[sequences];
        Arrays.fill(frameSpeed, 1); // inicializamos el arreglo frameSpeed
    }

    /**
     * Esteblece cada secuencia de animacion
     * @param nSequence
     * @param mode: mode de animacion
     * @param frameSpeed: velocidad
     * @param sequenceFrames: array de imagenes
     */
    void initSequence(int nSequence, int mode, int frameSpeed, BufferedImage sequenceFrames[]) {
        animationMode[nSequence] = mode;
        frames[nSequence] = sequenceFrames;
        this.frameSpeed[nSequence] = frameSpeed;
        endOfSequence = false;
    }

    /**
     * Metodo que cambia el state de sprite
     * @param state
     */
    void setState(int state) {
        this.state = state;
        if (state != STATE_INACTIVE) {
            curSequence = state - 1;
            curFrame = 0;
            activeFrames = frames[curSequence];
            frameCounter = frameSpeed[curSequence];
        }
    }

    // control de la velocidad actual y el sgte cuadro a dibujar en la pantalla
    void nextFrame() {
        frameCounter--;
        if (frameCounter <= 0) {
            // Siguiente cuadro
            if (animationMode[curSequence] == ANIMATION_SINGLESEQUENCE) {
                if (curFrame < (activeFrames.length-1))
                    curFrame++;
                else
                    endOfSequence = true;
            } else if (animationMode[curSequence] == ANIMATION_LOOP) {
                curFrame = (curFrame + 1) % activeFrames.length;
            }

            frameCounter = frameSpeed[curSequence];
        }
    }

    /**
     * metodo que informa al sprite que ha sido alcanzado con una bala
     */
    public void hitDetected() {
    }

    // metodo que dibuja un sprite
    void draw(Graphics2D g2d) {
        if (state == STATE_INACTIVE)
            return;
        if (blinkCount > 0) {
            // dibujamos la imagen con un nivel de transparencia. efecto que ocurre cuando
            // un enemigo recibe una bala que no es suficiente para matarlo
            Composite oldComposite = g2d.getComposite();
            g2d.setComposite(AlphaComposite.SrcAtop.derive(0.3f));
            // dibujamos la imagen pero ligeramente transparente
            g2d.drawImage(activeFrames[curFrame], x, y, null);
            g2d.setComposite(oldComposite);
            blinkCount--;
        } else
            // dibujamos el sprite
            g2d.drawImage(activeFrames[curFrame], x, y, null);
    }
}
