package com.ceva;

import java.awt.Point;
import java.awt.event.MouseEvent;
public class LineToolController extends EventController {
    private Point startPoint;
    private Point endPoint;
    private LevelView view;
    private LevelEditorModel model;

    public LineToolController(LevelView view, LevelEditorModel model) {
        this.view = view;
        this.model = model;
        reset();
    }

    @Override
    public void reset() {
        startPoint = new Point(0, 0);
        endPoint = new Point(0, 0);
        model.tmpLevelDataLen = 0;
    }

    /*
    Agrego en ar una lista de puntos que corresponden a la coordenada y de una
    linea entre (x1,y1) y (x2,y2).
    */
    private void drawStepLines(int x1, int y1, int x2, int y2) {
        int tmp;
        if (x2 < x1) {
            // swap(xa, xb)
            tmp = x1;
            x1 = x2;
            x2 = tmp;

            // swap(ya, yb)
            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }

        model.tmpLevelDataLen = x2 - x1 + 1;
        if ((model.tmpLevelData == null) || (model.tmpLevelData.length < model.tmpLevelDataLen)) {
            model.tmpLevelData = new short[model.tmpLevelDataLen];
        }

        float m = (float)(y2 - y1) / (x2 - x1);
        float y = y1;
        int idx = 0;
        for (int n=x1; n<=x2; n++) {
            model.tmpLevelData[idx++] = (short) y;
            y += m;
        }
    }

    @Override
    public boolean mousePressed(MouseEvent evt) {
        startPoint.x = evt.getX();
        startPoint.y = evt.getY();
        int height = model.maxHeight;
        if (startPoint.y >= height)
            startPoint.y = height - 1;
        model.tmpLevelDataX = startPoint.x;
        return true;
    }

    @Override
    public boolean mouseDragged(java.awt.event.MouseEvent evt) {
        endPoint.x = evt.getX();
        endPoint.y = evt.getY();
        if (endPoint.x < 0)
            endPoint.x = 0;
        if (endPoint.y < 0)
            endPoint.y = 0;
        else if (endPoint.y >= model.maxHeight)
            endPoint.y = model.maxHeight-1;

        drawStepLines(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        model.tmpLevelDataX = Math.min(startPoint.x, endPoint.x);
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