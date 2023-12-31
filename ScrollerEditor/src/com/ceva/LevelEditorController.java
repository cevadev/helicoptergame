package com.ceva;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Punto de inicio de la application
 * LevelEditorController configura el modelo, la vista, y muestra la ventana principal
 */
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

    /**
     * metod que coloca la variable curController al controlador secundario que le corresponde
     * @param currentTool
     */
    public void setCurrentTool(int currentTool) {
        // validamos si el nivel esta vacio
        if (model.levelH == null)
            return;
        model.currentTool = currentTool;
        if (currentTool == LevelEditorModel.TOOL_HELI) {
            curController = heliController;
            // llamamos al metodo init() del controlador secundario heliController
            ((HeliController)heliController).init();
        } else if (currentTool == LevelEditorModel.TOOL_FREE) {
            // cuando user selecciona el boton free, freeController pasa a ser el curController
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
            return; // si no es posible borrar lo que se encuentra en edicion, se cancela la accion

        // aparece ventana de dialogo que pregunta el nro de segundo que tendra el nivel
        CreateLevelDialog dlg = view.createLevelDialog();
        dlg.setVisible(true);// hacemos visible el dialogo
        // validamos si el user cancelo el dialogo
        if (!dlg.wasCancelled()) {
            // Si no cancelo el dialogo entonces Create a new level
            // le pedimos al modelo que cree el nivel porque es el modelo quien tiene la info del nivel
            model.createLevel(dlg.getNSeconds());
        }
    }

    public void doLoad() {
        // validamos si es seguro cargar el nuevo nivel y descartar los datos actuales
        if (!validateClose())
            return;

        File curDir = fc.getCurrentDirectory();
        // mostramos el dialog en modo abrir archivo
        int returnVal = fc.showOpenDialog(view.getMainFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            System.out.println("Load file: " + f);
            try {
                model.load(f);

                if (!curDir.equals(fc.getCurrentDirectory())) {
                    // guardamos el directorio que el user selecciono
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

    // Guardamos un nivel
    public boolean doSave() {
        // el campo currentFile del modelo indica cual es archivo que se esta editando
        // si currentFile es null significa que el archivo aun no existe
        if (model.currentFile == null) {
            // usamos la instancia de FileChooser creada
            // retornamos la respuesta del usuario
            int returnVal = fc.showSaveDialog(view.getMainFrame());
            // validamos si la respuesta fue un APPROVE_OPTION significa que el user selecciono un archivo
            // y presiono el boton de guardar
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                // obtenemos el archivo seleccionado en un objeto de tipo File
                File f = fc.getSelectedFile();
                System.out.println("Save to file: " + f);
                // validamos si el archivo ya existe
                if (f.exists()) {
                    // si elarchivo ya existe, le preguntamos si quiere sobrescribir el archivo
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
            // model.currentFile Ya tiene archivo
            try {
                model.save(model.currentFile); // el usuario esta guardando sus cambios
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

    // editamos las propiedades del enemgio
    private void configFoe(Foe foe) {
        DlgSetupFoe dlg = new DlgSetupFoe(view.getMainFrame());

        dlg.setAI(foe.ai);
        dlg.setStrength(foe.strengthLevel()); // establecemos la fuerza del enemigo
        dlg.setDisabledFire(foe.disabledFire);// deshabilitammos los disparos
        dlg.setVisible(true);

        // validamos que el no se haya seleccionado el boton cancel
        if (!dlg.wasCancelled()) {
            // obtenemos los valores del dialog y lo almacenamos en el foe
            model.updateFoe(foe, dlg.getAI(), dlg.getStrength(), dlg.getDisabledFire());
        }
    }

    /*
    mouseMoved sucede cuando el mouse pasa por el view sin que los botones mouse
    esten presionados.
    */
    public void mouseMoved(int x, int y) {
        view.updateStatusBar();
        // validamos si existe un controlador en curController y se encarga del evento
        if ((curController != null) && curController.mouseMoved(x, y))
            return; // metodo finaliza

        if (model.foes != null) {
            // vemos si hay un helicopter en la posicion actual del raton con model.findFoe(), si es asi
            // guardamos la referencia en la variable highLightedFoe
            model.setHighlightedFoe(model.findFoe(x, y));
        }
    }

    /*
    El boton ha sido presionado (no es un evento click).
    */
    public void mousePressed(java.awt.event.MouseEvent evt) {
        // si no hay herramienta seleccionada (TOOL_FREE, TOOL_LINE, TOOL_HELI)
        // entonces curController no maneja el evento. Pero si hay una herramienta seleccionada
        // llamamos al metodo mousePressed de dicha herramienta.
        if ((curController != null) && curController.mousePressed(evt))
            return;
        // validamos que el enemigo no se haya seleccionado
        if (model.highlightedFoe != null) {
            // seleccionamos la herramienta TOOL:HELI
            setCurrentTool(LevelEditorModel.TOOL_HELI);
            // selecciona herramienta heli, , heliController maneja el evento
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
        // si curController interpreta el evento, el metodo finaliza
        if ((curController != null) && curController.mouseClicked(evt))
            return;
        // como esta seleccionado heliController ejecutara su metodo mouseClick()
        // buscamos si hay un enemigo en la posicion del raton
        Foe f = model.findFoe(evt.getX(), evt.getY());
        if (model.selectedFoe != f) {
            // seleccionamos y resaltamos al enemigo
            model.setSelectedFoe(f);
            model.setHighlightedFoe(f);
        }
        // validamos si es un doble click
        if ((evt.getClickCount() == 2) && (model.highlightedFoe != null)) {
            // al hacer doble click al enemigo, permitimos editar las propiedades del enemigo
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
        // el user presiono scape se hace un reset el controlador, sin importar cual sea
        if (evt.getKeyChar() == KeyEvent.VK_ESCAPE) {
            if (curController != null)
                curController.reset();
            model.setSelectedFoe(null); // informamos que se redibuje la vista
        }
        // si se presiona backespace o delete y selected foe null
        else if (((evt.getKeyChar() == KeyEvent.VK_BACK_SPACE) || (evt.getKeyChar() == KeyEvent.VK_DELETE)) &&
                (model.selectedFoe != null)) {
            model.removeFoe(model.selectedFoe); // eliminamos al enemigo
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

    /*
     * funcion que retorna true si es posible en ese momentoborrar lo que se encuentre
     * en edicion. Si retorna false, entonce se cancela la ejecucion del comando
     */
    protected boolean validateClose() {
        // validamos si model.dirty es true, que indica que se hicieron cambios en el nivel
        if (model.dirty) {
            // informamos al usuario de cambios pendientes
            // yes - queremos guardar antes de continuar
            // no - queremos continuar y no importa perder los cambios
            // cancel - ya no queremos continuar con la creacion
            int confirm = view.yesNoCancelConfirmation("¿Deseas guardar los cambios?", "Salir");
            if (confirm == JOptionPane.YES_OPTION) {
                // doSave() retorna false si la operacion de guardar el nivel no se puedo realizar
                if (!doSave())
                    return false;
            } else if (confirm == JOptionPane.CANCEL_OPTION)
                return false;
        }
        return true; // se ha guardado correctamente los datos, y se puede borrar info del nivel
    }
}
