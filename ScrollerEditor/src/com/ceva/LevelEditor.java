package com.ceva;

import javax.swing.UIManager;
public class LevelEditor {
    public static void main(String args[]) {
        try {
            javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
        }

        java.awt.EventQueue.invokeLater(() -> {
            Util.defaultClassPath = "/com/ceva/";

            // creamos el modelo
            LevelEditorModel model = new LevelEditorModel();
            // creamos la vista
            LevelEditorView view = new LevelEditorView();
            // pasamos al controlador model y view
            LevelEditorController controller = new LevelEditorController(model, view);
            controller.start();
        });
    }
}
