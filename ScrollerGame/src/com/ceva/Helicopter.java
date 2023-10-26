package com.ceva;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Helicopter {
    private KeyBoard keyboard;
    // donde cada constante representa un bit izq, derecha, arriba, abajo
    private static int JLEFT  = 1;
    private static int JRIGHT = 2;
    private static int JUP    = 4;
    private static int JDOWN  = 8;
    // variable que guarda el estabado de todas las direcciones
    private int joystickState = 0;

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
}
