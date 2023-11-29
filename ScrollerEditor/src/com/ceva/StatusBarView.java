package com.ceva;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class StatusBarView extends JPanel {
    private LevelEditorModel model;
    private LevelView levelView;

    public void setModel(LevelEditorModel model) {
        this.model = model;
    }

    public void setLevelView(LevelView levelView) {
        this.levelView = levelView;
        this.model = levelView.getModel();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        FontMetrics fm = g2d.getFontMetrics();

        String curTool;
        switch (model.currentTool) {
            case LevelEditorModel.TOOL_FREE:
                curTool = "FREE";
                break;
            case LevelEditorModel.TOOL_LINE:
                curTool = "LINE";
                break;
            case LevelEditorModel.TOOL_HELI:
                curTool = "HELI";
                break;
            default:
                curTool = "None";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Tool: %s", curTool));

        int x = 6;
        int y = fm.getAscent();
        g2d.drawString(sb.toString(), x, y);
        if (levelView != null) {
            String str = String.format("view width: %d", levelView.getVisibleRect().width);
            y += fm.getHeight();
            g2d.drawString(str, x, y);
        }
        y += fm.getHeight();
        g2d.drawString(String.format("Mouse: (%d,%d)", levelView.curMouseX, levelView.curMouseY), x, y);

        x = getBounds().width / 2;
        y = fm.getAscent();
        if (model.levelH != null)
            g2d.drawString(String.format("Level size: %d secs.", model.levelH.length/60), x, y);
        if (model.currentFile != null) {
            y += fm.getHeight();
            g2d.drawString(String.format("Current file: %s", model.currentFile.getName()), x, y);
        }
    }

}
