package com.ceva;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Screen {
    public static final int FPS = 60;
    private int width;
    private int height;
    private BufferedImage screenBuffer;
    private Graphics2D g2d;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        screenBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g2d = screenBuffer.createGraphics();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Graphics2D getGraphics2D() {
        return g2d;
    }

    public void paint(Graphics2D g2d) {
        g2d.drawImage(screenBuffer, 0, 0, null);
    }

    public void done() {
        g2d.dispose();
        screenBuffer = null;
        g2d = null;
    }
}
