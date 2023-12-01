package com.ceva;

import javax.swing.UIManager;

/**
 * Clase que maneja la referencias a las vistas
 */
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
            // creamos la vista principal
            LevelEditorView view = new LevelEditorView();
            // el controlador se encarga de resolver todas las dependecias entre model view y controller
            LevelEditorController controller = new LevelEditorController(model, view);
            controller.start();
        });
    }
}
