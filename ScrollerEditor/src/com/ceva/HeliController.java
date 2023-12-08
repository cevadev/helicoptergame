package com.ceva;

import java.awt.event.MouseEvent;
public class HeliController extends EventController {
    private LevelEditorModel model;
    private LevelView view;
    private LevelEditorController mainController;
    private int foeDX;
    private int foeDY;

    public HeliController(LevelEditorModel model, LevelView view, LevelEditorController mainController) {
        this.model = model;
        this.view = view;
        this.mainController = mainController;
    }

    public void init() {
        if (model.highlightedFoe == null) {
            // inicializamos las variables que indican la posicion del helicoptero que esta el cursor
            // el nuevo helicopter aparecera horizontalmente a la derecha donde este el raton
            // y verticalmente centrado
            foeDX = 0;
            foeDY = 32;
        }
    }

    @Override
    public boolean mousePressed(MouseEvent evt) {
        if (model.highlightedFoe != null) {
            model.selectedFoe = model.highlightedFoe;
            foeDX = evt.getX() - model.selectedFoe.x;
            foeDY = evt.getY() - model.selectedFoe.y;
            model.curSpriteX = -1;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        if (model.selectedFoe == null) {
            // pasamos las coordenadas del raton
            model.curSpriteX = x - foeDX;
            model.curSpriteY = y - foeDY;
            if (model.curSpriteY > model.maxHeight) {
                model.curSpriteX = -1;
            }
            view.repaint(); // redibujamos la vista
        }
        return true; // informamos que el evento fue manejado
    }

    @Override
    public boolean mouseDragged(MouseEvent evt) {
        if (model.selectedFoe != null) {
            // Estoy moviendo un objeto
            model.selectedFoe.x = evt.getX() - foeDX;
            model.selectedFoe.y = evt.getY() - foeDY;
            model.dirty = true;
            view.repaint();
            return true;
        }
        return false;
    }

    // click con el mouse significa que el usuario quiere agregar un enemigo
    @Override
    public boolean mouseClicked(MouseEvent evt) {
        if (model.selectedFoe == null) {
            // agrego un enemigo al controlador
            model.addFoe(model.curSpriteX, model.curSpriteY);
            // informamos al controlador principal que ya no estara seleccionado ningun controller secundario
            mainController.setCurrentTool(LevelEditorModel.TOOL_NONE);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseEvent evt) {
        if (model.selectedFoe != null) {
            // Reordenar foe
            model.reOrderFoe(model.selectedFoe);
            // deshabilitamos la herramienta actual
            mainController.setCurrentTool(LevelEditorModel.TOOL_NONE);
        }
        model.selectedFoe = null;
        return true;
    }

    @Override
    public boolean mouseExited(MouseEvent evt) {
        model.curSpriteX = -1;
        view.repaint();
        return true;
    }

    @Override
    public void reset() {
        model.selectedFoe = null;
        mainController.setCurrentTool(LevelEditorModel.TOOL_NONE);
    }

}
