package com.ceva;

import java.awt.event.MouseEvent;
public class FreeToolController extends EventController {
    private LevelView view;
    private LevelEditorModel model;

    // lastX, lastY controlan el dibujado, representan la ultima posicion que tuvo el raton
    // y asi conocer como se mueve entre cada evento de arrastre
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
            // invertimos los puntos en caso de que la posicion final, se encuentre antes que la inicial
            // swap(xa, xb)
            int tmp = x1;
            x1 = x2;
            x2 = tmp;

            // swap(ya, yb)
            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }

        // cuando tmpLevelData no es null, lo que debemos hacer es agrandar tmpLevelData si es necesario
        if (model.tmpLevelData != null) {
            // variables que representan espacio faltante de izq y derecha para agrandar tmpLevelData
            int extraLeft = 0;
            int extraRight = 0;
            // validamo si x1 es menor al punto inicial (model.tmpLevelDataX)
            // si tmpLevelDataX igual a 100, es decir vamos a dibujar a partir de la coordenada 100
            // pero resulta que la nueva linea va a empezar en 90, entonces x1 (90) es mejor que
            // model.tmpLevelDataX (100) significa que hay un espacio faltante a la izq de tamano 10
            if (x1 < model.tmpLevelDataX)
                // obtenemos el tamano del espacio faltante
                extraLeft = model.tmpLevelDataX - x1;
            if (x2 >= (model.tmpLevelDataX + model.tmpLevelDataLen))
                // obtenemos el faltante hacia la derecha
                extraRight = x2 - (model.tmpLevelDataX + model.tmpLevelDataLen) + 1;
            // validamos i extrLeft o extrRight son mayores a 0
            if ((extraLeft != 0) || (extraRight != 0)) {
                // agrandamos tmpLevelData
                if (model.tmpLevelData.length < (model.tmpLevelDataLen + extraLeft + extraRight)) {
                    // Agrandar arreglo
                    short newData[] = new short[model.tmpLevelDataLen + extraLeft + extraRight];
                    System.arraycopy(model.tmpLevelData, 0, newData, extraLeft, model.tmpLevelDataLen);
                    // redimensionamos el array tmpLevelData
                    model.tmpLevelData = newData;
                    model.tmpLevelDataLen = model.tmpLevelData.length;
                } else
                // si hay un faltante de informacion pero el tamano de tmpLevelData es suficientemente grande
                {
                    if (extraLeft > 0) {
                        // recorremos los datos existentes hacia la derecha
                        for (int n=model.tmpLevelDataLen-1; n>=0; n--)
                            model.tmpLevelData[n+extraLeft] = model.tmpLevelData[n];
                    }
                    model.tmpLevelDataLen += extraLeft + extraRight;
                }
            }
        } else {
            // creamos un arreglo
            model.tmpLevelData = new short[x2 - x1 + 1];
            // inicializamos tmpLevelDataLen
            model.tmpLevelDataLen = model.tmpLevelData.length;
        }
        if (x1 < model.tmpLevelDataX)
            model.tmpLevelDataX = x1;

        // dibujamos la linea
        float m = (float)(y2 - y1) / (x2 - x1);
        float y = y1;
        for (int n=x1; n<=x2; n++) {
            model.tmpLevelData[n - model.tmpLevelDataX] = (short) y;
            y += m;
        }
    }

    // inicalizamos los valores
    @Override
    public void reset() {
        model.tmpLevelDataX = 0;
        lastX = 0;
        lastY = 0;
        model.tmpLevelDataLen = 0; // significa que no hay info de terreno en edicion
    }

    /**
     * Metodo que guarda el punto inicial del mouse
     * este evento se dispara cuando el usuario presiona el boton izquierdo del raton
     * a este metodo llegamos:
     * Despues de haber seleccionado la herramienta desde la vista. se presiona el boton
     * izq del raton dentro del area de edicion del terreno (LevelView).
     * En el constructor de LevelView se manejan todos los eventos del raton, cuando se
     * presiona el boton izq del raton, se dispara el metodo mousePressed() dicho evento
     * llama a controller.mousePressed que es el LevelEditorController, como ya se selecciono
     * la herramienta TOOL_FREE de la vista, curController (LevelEdiorController)
     * va apuntar a FreeToolController y es asi como llegamos a este metodo
     */
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

    /**
     * Este metodo se dispara cuando el usuario arrastra el raton para dibujar. Para cuando se
     * dispara este evento, ya sabemos que el raton se desplazo desde lastX, lastY hasta
     * curX, CurY y asi podemos dibujar una linea entre esos dos puntos
     */
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
        // validamos que curY no sea mayor a la altura del nivel, esto puede pasar si agradamos
        // verticalmente la ventana a un tamano mas grande que el nivel de edicion
        else if (curY >= model.maxHeight)
            curY = model.maxHeight - 1;

        // Marcar linea de lastX a curX
        drawStepLines(lastX, lastY, curX, curY);
        // actualizamos las variables de la posicion actual del mouse, preparandonos para el sgte
        // evento de arrastre
        lastX = curX;
        lastY = curY;
        view.repaint();
        return true;
    }

    // dibujamos la linea permanente cuando se libera el mouse
    @Override
    public boolean mouseReleased(MouseEvent evt) {
        model.writeTmpLevelData();
        reset();
        return true;
    }

}
