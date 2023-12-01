package com.ceva;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
public class LevelEditorView {
    private JFrame mainFrame;
    private LevelView levelView;
    private StatusBarView statusBar;

    public LevelEditorView() {
        MainFrame frame = new MainFrame();

        mainFrame = frame;
        levelView = frame.getView();
        statusBar = frame.getStatusBar();
    }

    public void setModel(LevelEditorModel model) {
        levelView.setModel(model);// asignamos el modelo a todas las vistas
        statusBar.setModel(model);
    }

    public void setController(LevelEditorController controller) {
        levelView.setController(controller);
    }

    public JFrame getMainFrame() {
        return mainFrame;
    }

    public LevelView getLevelView() {
        return levelView;
    }

    public CreateLevelDialog createLevelDialog() {
        return new CreateLevelDialog(mainFrame);
    }

    public DlgSetupFoe createSetupFoeDialog() {
        return new DlgSetupFoe(mainFrame);
    }

    public void repaint() {
        levelView.repaint();
    }

    public void updateStatusBar() {
        statusBar.repaint();
    }

    public void requestFocusInWindow() {
        levelView.requestFocusInWindow();
    }

    public int yesNoConfirmation(String message) {
        return JOptionPane.showConfirmDialog(mainFrame, message, "Confirmación", JOptionPane.YES_NO_OPTION);
    }

    public int yesNoCancelConfirmation(String message, String confirmTitle) {
        return JOptionPane.showConfirmDialog(mainFrame, message, confirmTitle, JOptionPane.YES_NO_CANCEL_OPTION);
    }

    public JFileChooser getFileChooser() {
        JFileChooser res = new JFileChooser();
        res.setFileFilter(new FileFilter() {
            // filtramos los archivos que queremos mostrar (.DAT)
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toUpperCase().endsWith(".DAT");
            }

            @Override
            public String getDescription() {
                return ".DAT files";
            }
        });
        // guardamos el ultimo directorio guardado por el usuario
        String strCurDir = AppSettings.getInstance().getString("scrollerEditor.curDir");
        if (strCurDir != null) {
            File f = new File(strCurDir);
            if (f.exists() && f.isDirectory()) {
                res.setCurrentDirectory(f);
            }
        }
        return res;
    }
}

