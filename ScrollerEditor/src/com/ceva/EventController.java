package com.ceva;

import java.awt.event.MouseEvent;
public abstract class EventController {
    public boolean mouseMoved(int x, int y) {
        return false;
    }
    public boolean mousePressed(java.awt.event.MouseEvent evt) {
        return false;
    }
    public boolean mouseDragged(java.awt.event.MouseEvent evt) {
        return false;
    }
    public boolean mouseReleased(java.awt.event.MouseEvent evt) {
        return false;
    }
    public boolean mouseClicked(MouseEvent evt) {
        return false;
    }
    public boolean mouseExited(MouseEvent evt) {
        return false;
    }
    public abstract void reset();
}
