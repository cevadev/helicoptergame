package com.ceva;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
public class LevelEditorController {
    protected LevelEditorModel model; // refrencia al modelo
    protected LevelEditorView view; // refrencia a la vista
    protected LevelView levelView;

    private JFileChooser fc;

    private EventController curController;
    private EventController heliController;
    private EventController freeController;
    private EventController lineController;

    /**
     *  El controlador no dependen de objetos de tipo JFrame
     */
    public LevelEditorController(LevelEditorModel model, LevelEditorView view) {
        // guardamos una referencia al model y view
        // La vista necesita del modelo para poder mostrar la info
        this.model = model;
        this.view = view;
        levelView = view.getLevelView();
        init();
    }

    // establecemos las referencias al model y controller. Resolvemos lo que hace el constructor
    private void init() {
        view.setModel(model); // a la vista le pasamos el modelo
        view.setController(this); // a la vista le pasamos el controlador

        // por medio de observers el model informa cuando han ocurrido cambios
        model.addObserver(levelView);// relacionamos el modelo con la vista
        setUpOrDown(true);

        // guardamos la instancia de un objto JFileChooser para utilizarlos
        // cada vez que se quiera abrir o guardar un archivo
        fc = view.getFileChooser();

        /**
         * Creamos 3 subcontroladores
         * Cuando nuestro controlador reciba un mensaje de la vista, asu vez redireccionara
         * el mensaje a uno de estos subcontroladores secundarios que maneja la funcionalidad
         * para agregar terreno y enemigos
         */
        heliController = new HeliController(model, levelView, this);
        freeController = new FreeToolController(levelView, model);
        lineController = new LineToolController(levelView, model);
    }

    public void setCurrentTool(int currentTool) {
        if (model.levelH == null)
            return;
        model.currentTool = currentTool;
        if (currentTool == LevelEditorModel.TOOL_HELI) {
            curController = heliController;
            ((HeliController)heliController).init();
        } else if (currentTool == LevelEditorModel.TOOL_FREE) {
            curController = freeController;
            ((FreeToolController)curController).reset();
        } else if (currentTool == LevelEditorModel.TOOL_LINE) {
            curController = lineController;
            ((LineToolController)curController).reset();
        } else
            curController = null;

        view.updateStatusBar();// re dibujamos statusBar
        if (curController != null)
            view.requestFocusInWindow();// reconocemos los eventos del teclado
    }

    protected void createNewLevel() {
        // Crear un nuevo nivel.
        if (!validateClose())
            return;

        CreateLevelDialog dlg = view.createLevelDialog();
        dlg.setVisible(true);// hacemos visible el dialogo
        if (!dlg.wasCancelled()) {
            // Create a new level
            model.createLevel(dlg.getNSeconds());
        }
    }

    public void doLoad() {
        if (!validateClose())
            return;

        File curDir = fc.getCurrentDirectory();
        int returnVal = fc.showOpenDialog(view.getMainFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            System.out.println("Load file: " + f);
            try {
                model.load(f);

                if (!curDir.equals(fc.getCurrentDirectory())) {
                    AppSettings.getInstance()
                            .setString("scrollerEditor.curDir", fc.getCurrentDirectory().getAbsolutePath())
                            .save();
                }
            } catch (IOException e) {
                System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public boolean doSave() {
        if (model.currentFile == null) {
            // usamos la instancia de FileChooser creada
            int returnVal = fc.showSaveDialog(view.getMainFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                System.out.println("Save to file: " + f);
                if (f.exists()) {
                    // le pasamos a la vista la funcionalidad
                    if (view.yesNoConfirmation("¿Deseas sobreescribir el archivo?") != JOptionPane.NO_OPTION)
                        return false;
                }

                try {
                    model.save(f);
                    return true;
                } catch (IOException e) {
                    System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            // Ya existe el archivo
            try {
                model.save(model.currentFile);
                return true;
            } catch (IOException e) {
                System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    public void doSaveAs() {
        int returnVal = fc.showSaveDialog(view.getMainFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            System.out.println("Save to file: " + f);
            if (f.exists()) {
                if (JOptionPane.showConfirmDialog(null, "Deseas sobreescribir el archivo?", "Confirmacion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                    return;
            }

            try {
                model.save(f);
            } catch (IOException e) {
                System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void configFoe(Foe foe) {
        DlgSetupFoe dlg = new DlgSetupFoe(view.getMainFrame());

        dlg.setAI(foe.ai);
        dlg.setStrength(foe.strengthLevel());
        dlg.setDisabledFire(foe.disabledFire);
        dlg.setVisible(true);

        if (!dlg.wasCancelled()) {
            model.updateFoe(foe, dlg.getAI(), dlg.getStrength(), dlg.getDisabledFire());
        }
    }

    /*
    mouseMoved sucede cuando el mouse pasa por el view sin que los botones
    esten presionados.
    */
    public void mouseMoved(int x, int y) {
        view.updateStatusBar();
        if ((curController != null) && curController.mouseMoved(x, y))
            return;

        if (model.foes != null) {
            model.setHighlightedFoe(model.findFoe(x, y));
        }
    }

    /*
    El boton ha sido presionado (no es un evento click).
    */
    public void mousePressed(java.awt.event.MouseEvent evt) {
        if ((curController != null) && curController.mousePressed(evt))
            return;

        if (model.highlightedFoe != null) {
            setCurrentTool(LevelEditorModel.TOOL_HELI);
            heliController.mousePressed(evt);
        }
    }

    /*
    mouseDragged se genera cuando estando presionado el boton, se mueve el mouse.
    */
    public void mouseDragged(java.awt.event.MouseEvent evt) {
        view.updateStatusBar();
        if (curController != null)
            curController.mouseDragged(evt);
    }

    /*
    El boton dejo de ser presionado. Este evento se recibe aunque el mouse se
    encuentre fuera de este JPanel incluso fuera de la ventana.
    */
    public void mouseReleased(java.awt.event.MouseEvent evt) {
        if (curController != null)
            curController.mouseReleased(evt);
    }

    /*
    mouse clicked se genera cuando se presiona y se suelta el boton.
    */
    public void mouseClicked(java.awt.event.MouseEvent evt) {
        if ((curController != null) && curController.mouseClicked(evt))
            return;

        Foe f = model.findFoe(evt.getX(), evt.getY());
        if (model.selectedFoe != f) {
            model.setSelectedFoe(f);
            model.setHighlightedFoe(f);
        }

        if ((evt.getClickCount() == 2) && (model.highlightedFoe != null)) {
            configFoe(model.highlightedFoe);
        }
    }

    public void mouseEntered(java.awt.event.MouseEvent evt) {
        if (curController != null)
            view.requestFocusInWindow();
    }

    public void mouseExited(java.awt.event.MouseEvent evt) {
        if (curController != null)
            curController.mouseExited(evt);
    }

    public void keyTyped(KeyEvent evt) {
        if (evt.getKeyChar() == KeyEvent.VK_ESCAPE) {
            if (curController != null)
                curController.reset();
            model.setSelectedFoe(null);
        } else if (((evt.getKeyChar() == KeyEvent.VK_BACK_SPACE) || (evt.getKeyChar() == KeyEvent.VK_DELETE)) &&
                (model.selectedFoe != null)) {
            model.removeFoe(model.selectedFoe);
        }
    }

    public void setUpOrDown(boolean upOrDown) {
        model.upOrDown = upOrDown;
    }

    public EventController getCurrentController() {
        return curController;
    }

    // metodo que hace visible la pantalla principal
    public void start() {
        view.getMainFrame().setVisible(true);
    }

    protected boolean validateClose() {
        if (model.dirty) {
            int confirm = view.yesNoCancelConfirmation("¿Deseas guardar los cambios?", "Salir");
            if (confirm == JOptionPane.YES_OPTION) {
                if (!doSave())
                    return false;
            } else if (confirm == JOptionPane.CANCEL_OPTION)
                return false;
        }
        return true;
    }
}
