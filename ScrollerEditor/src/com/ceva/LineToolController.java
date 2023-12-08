package com.ceva;

import java.awt.Point;
import java.awt.event.MouseEvent;

// Clase que permite dibujar linealmente
public class LineToolController extends EventController {
    // startPoint y endPoint, representan punto inicial y final de la linea a dibujar.
    private Point startPoint; // se establece al hacer click en el boton izq del raton
    private Point endPoint; // se establece cuando se arrastra el mouse
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
        // validamos si el tamano de tmpLevelData alcanza, si no, lo redimensionamos
        if ((model.tmpLevelData == null) || (model.tmpLevelData.length < model.tmpLevelDataLen)) {
            model.tmpLevelData = new short[model.tmpLevelDataLen];
        }
        // algoritmo para dibujar una linea
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
        // establecemos el punto inicial donde el raton fue presionado
        startPoint.x = evt.getX();
        startPoint.y = evt.getY();
        int height = model.maxHeight;
        // validamos que la coordenada y no sea mayor al maximo tamano del nivel
        if (startPoint.y >= height)
            startPoint.y = height - 1;
        model.tmpLevelDataX = startPoint.x;
        return true;
    }

    @Override
    public boolean mouseDragged(java.awt.event.MouseEvent evt) {
        // determinamos el punto final
        endPoint.x = evt.getX();
        endPoint.y = evt.getY();
        // validamos que las posiciones se encuentren dentor de los rangos aceptables
        if (endPoint.x < 0)
            endPoint.x = 0;
        if (endPoint.y < 0)
            endPoint.y = 0;
        else if (endPoint.y >= model.maxHeight)
            endPoint.y = model.maxHeight-1;
        // dibujamos la linea de edicion temporal
        drawStepLines(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
        // actualizamos el modelo con la coordenada mas pequena de x entre el punto inicial y el final
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