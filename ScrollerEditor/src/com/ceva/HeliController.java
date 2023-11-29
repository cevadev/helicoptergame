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
            model.curSpriteX = x - foeDX;
            model.curSpriteY = y - foeDY;
            if (model.curSpriteY > model.maxHeight) {
                model.curSpriteX = -1;
            }
            view.repaint();
        }
        return true;
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

    @Override
    public boolean mouseClicked(MouseEvent evt) {
        if (model.selectedFoe == null) {
            model.addFoe(model.curSpriteX, model.curSpriteY);
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
