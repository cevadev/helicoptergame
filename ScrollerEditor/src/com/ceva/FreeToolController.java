package com.ceva;

import java.awt.event.MouseEvent;
public class FreeToolController extends EventController {
    private LevelView view;
    private LevelEditorModel model;

    private int lastX;
    private int lastY;

    public FreeToolController(LevelView view, LevelEditorModel model) {
        this.view = view;
        this.model = model;
        reset();
    }

    /*
    Agrego en data una lista de puntos que corresponden a la coordenada y de una
    linea entre (x1,y1) y (x2,y2).
    */
    private void drawStepLines(int x1, int y1, int x2, int y2) {
        if (x2 < x1) {
            // swap(xa, xb)
            int tmp = x1;
            x1 = x2;
            x2 = tmp;

            // swap(ya, yb)
            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }

        if (model.tmpLevelData != null) {
            int extraLeft = 0;
            int extraRight = 0;
            if (x1 < model.tmpLevelDataX)
                extraLeft = model.tmpLevelDataX - x1;
            if (x2 >= (model.tmpLevelDataX + model.tmpLevelDataLen))
                extraRight = x2 - (model.tmpLevelDataX + model.tmpLevelDataLen) + 1;
            if ((extraLeft != 0) || (extraRight != 0)) {
                if (model.tmpLevelData.length < (model.tmpLevelDataLen + extraLeft + extraRight)) {
                    // Agrandar arreglo
                    short newData[] = new short[model.tmpLevelDataLen + extraLeft + extraRight];
                    System.arraycopy(model.tmpLevelData, 0, newData, extraLeft, model.tmpLevelDataLen);
                    model.tmpLevelData = newData;
                    model.tmpLevelDataLen = model.tmpLevelData.length;
                } else {
                    if (extraLeft > 0) {
                        for (int n=model.tmpLevelDataLen-1; n>=0; n--)
                            model.tmpLevelData[n+extraLeft] = model.tmpLevelData[n];
                    }
                    model.tmpLevelDataLen += extraLeft + extraRight;
                }
            }
        } else {
            model.tmpLevelData = new short[x2 - x1 + 1];
            model.tmpLevelDataLen = model.tmpLevelData.length;
        }
        if (x1 < model.tmpLevelDataX)
            model.tmpLevelDataX = x1;

        float m = (float)(y2 - y1) / (x2 - x1);
        float y = y1;
        for (int n=x1; n<=x2; n++) {
            model.tmpLevelData[n - model.tmpLevelDataX] = (short) y;
            y += m;
        }
    }

    @Override
    public void reset() {
        model.tmpLevelDataX = 0;
        lastX = 0;
        lastY = 0;
        model.tmpLevelDataLen = 0;
    }

    @Override
    public boolean mousePressed(MouseEvent evt) {
        lastX = evt.getX();
        lastY = evt.getY();
        model.tmpLevelDataX = lastX;
        if (lastY < 0)
            lastY = 0;
        else if (lastY >= model.maxHeight)
            lastY = model.maxHeight - 1;
        model.tmpLevelDataLen = 0;
        return true;
    }

    @Override
    public boolean mouseDragged(MouseEvent evt) {
        int curX = evt.getX();
        int curY = evt.getY();
        if (curX < 0)
            curX = 0;
        else if (curX > model.levelH.length)
            curX = model.levelH.length - 1;
        if (curY < 0)
            curY = 0;
        else if (curY >= model.maxHeight)
            curY = model.maxHeight - 1;

        // Marcar linea de lastX a curX
        drawStepLines(lastX, lastY, curX, curY);
        lastX = curX;
        lastY = curY;
        view.repaint();
        return true;
    }

    @Override
    public boolean mouseReleased(MouseEvent evt) {
        model.writeTmpLevelData();
        reset();
        return true;
    }

}
