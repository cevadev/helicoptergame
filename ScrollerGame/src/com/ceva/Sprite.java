package com.ceva;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Clase que realiza la animacion de imagenes
 */
public class Sprite {
    int state;      // Estado del sprite (STATE_ACTIVE, STATE_DYING, ...)
    int x;          // Coordenada X donde se dibuja.
    int y;          // Coordenada Y donde se dibuja.
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
    int curFrame;         // imagen actual en activeFrames.

    BufferedImage frames[][];
    BufferedImage activeFrames[];
    int frameSpeed[];     // Numero de cuadros antes de cambiar de imagen.
    int animationMode[];  /* 0 = La animacion se queda fija en el ultimo cuadro.
                             1 = La animacion se repite despues del ultimo cuadro.
                          */
    boolean endOfSequence; // true cuando ya se acabo la animacion.
}
