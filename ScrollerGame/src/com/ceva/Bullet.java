package com.ceva;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
public class Bullet {
    final static int BULLET_ACTIVE = 1; // primer bit
    final static int BULLET_FOE = 2; // bit 0, se trata del usuario y 1 cuando es bala enemiga
    static BufferedImage img; // todas las balas compartiran la misma instancia del dibujo de bala

    /* Una bala puede estar activa y ser del jugador o del enemigo. codificado en bit */
    int state;
    int x;
    int y;
    // contenemos el # de pixel que se desplazara la bala horizontal y vertical
    int dx;
    int dy;

    public Bullet() {
        if (img == null) {
            img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.YELLOW);
            g2d.fillRect(0, 0, 4, 4);
            img.setRGB(0, 0, 0x0000ff);
            img.setRGB(3, 0, 0x0000ff);
            img.setRGB(0, 3, 0x0000ff);
            img.setRGB(3, 3, 0x0000ff);
            g2d.dispose();
        }
    }

    void draw(Graphics2D g2d) {
        if ((state & Bullet.BULLET_ACTIVE) != 0) {
            g2d.drawImage(img, x-2, y-2, null);
        }
    }
}
