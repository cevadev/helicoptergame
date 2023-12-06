package com.ceva;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * Representa la vista donde se crear lo niveles. Es un JPanel que esta dentro de una JScrollPane
 */
public class LevelView extends JPanel implements Scrollable, ModelObserver {
    private LevelEditorModel model;
    private LevelEditorController controller;
    StatusBarView statusBar;
    Dimension maxDimension = new Dimension(0, 0);
    Dimension curDimension = new Dimension(maxDimension);
    // Coordenadas que indican la posicion del raton y se muestra en el statuspanel
    int curMouseX;
    int curMouseY;

    public LevelView() {
        super();

        setFocusable(true);
        /**
         * La vista al detectar una accion de usuario la reportamos al controlador
         */
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.mouseClicked(e);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                controller.mousePressed(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                controller.mouseReleased(e);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                controller.mouseEntered(e);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                controller.mouseExited(e);
            }
            // se ejecuta cuando movemos el raton sobre la pantalla, sin presionar boton del mouse
            @Override
            public void mouseMoved(MouseEvent e) {
                // guardamos la posicion del raton
                curMouseX = e.getX();
                curMouseY = e.getY();
                controller.mouseMoved(curMouseX, curMouseY);
            }
            // se ejecuta cuando arrastramos el raton sobre la pantalla presionando el boton izq del mouse
            @Override
            public void mouseDragged(MouseEvent e) {
                // guardamos la posicion del raton
                curMouseX = e.getX();
                curMouseY = e.getY();
                controller.mouseDragged(e);
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                controller.keyTyped(e);
            }
        });
    }

    public void setController(LevelEditorController controller) {
        this.controller = controller;
    }

    public LevelEditorController getController() {
        return controller;
    }

    public LevelEditorModel getModel() {
        return model;
    }

    public void setModel(LevelEditorModel model) {
        this.model = model;
    }

    public void setStatusBar(StatusBarView statusBar) {
        this.statusBar = statusBar;
    }

    public void setMaxDimension(int width, int height) {
        maxDimension.width = width;
        maxDimension.height = height;

        // metodo de swing: informamos de que tamano queremos que sea ahora el JPanel
        setPreferredSize(maxDimension);
        // metodo de swing: se recalculan el tamano de los componentes de la ventana
        revalidate();
        repaint(); // se vuelve a dibujar el componente
    }

    private void paintEditingGround(Graphics2D g2d) {
        // Paint editing information
        if (model.tmpLevelDataLen == 0)
            return;
        g2d.setColor(Color.WHITE);

        Rectangle visibleRect = getVisibleRect();
        if ((model.tmpLevelDataX < (visibleRect.x+visibleRect.width)) && ((model.tmpLevelDataX + model.tmpLevelDataLen) >= visibleRect.x)) {
            int n = 0;
            int x = model.tmpLevelDataX;
            int size = model.tmpLevelDataLen;
            if (x < visibleRect.x) {
                n = visibleRect.x - model.tmpLevelDataX;
                x = visibleRect.x;
                size -= n;
            }
            if ((x + size) >= (visibleRect.x + visibleRect.width)) {
                size -= (x + size) - (visibleRect.x + visibleRect.width);
            }

            if (model.upOrDown) {
                while (size > 0) {
                    g2d.drawLine(x, 0, x, model.tmpLevelData[n++]);
                    x++;
                    size--;
                }
            } else {
                while (size > 0) {
                    g2d.drawLine(x, model.tmpLevelData[n++], x, model.maxHeight);
                    x++;
                    size--;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK); //limpiamos la pantalla
        //obtenemos el rectangulo que define el area de la ventana que se encuentra visible
        Rectangle visibleRect = getVisibleRect();
        g2d.fillRect(visibleRect.x, visibleRect.y, visibleRect.width, visibleRect.height);

        if (model.levelH != null) {
            // Terreno
            g2d.setColor(Color.blue);
            g2d.fillRect(0, 0, model.levelH.length, model.maxHeight);
            g2d.setColor(Color.yellow);
            int len = visibleRect.width;
            if (len > model.levelH.length)
                len = model.levelH.length;
            // dibujamos terreno
            for (int n=visibleRect.x; n<(visibleRect.x + len); n++) {
                g2d.drawLine(n, 0, n, model.levelH[n]);
                g2d.drawLine(n, model.levelL[n], n, model.maxHeight);
            }

            // Editor de terreno
            paintEditingGround(g2d);

            // Para cada elemento dibujamos la imagen del Enemigos
            if (model.foes != null) {
                Foe foe = model.foes;
                while (foe != null) {
                    // verificamos que sus coordenadas se encuentren dentro del area visible de pantalla
                    if ((foe.x >= visibleRect.x-model.heliImage.getWidth()) && (foe.x <= (visibleRect.x + visibleRect.width))) {
                        if (foe.type == Foe.FOE_TYPE_HELI) {
                            g2d.drawImage(model.heliImage, foe.x, foe.y, null);
                        }
                        if ((foe == model.highlightedFoe) || (foe == model.selectedFoe)) {
                            if (foe == model.highlightedFoe)
                                g2d.setColor(Color.ORANGE);
                            else
                                g2d.setColor(Color.WHITE);
                            g2d.drawRect(foe.x, foe.y, 64, 64);
                        }
                    }
                    foe = foe.next;
                }
            }
            // Herramienta de helicoptero
            if (model.currentTool == LevelEditorModel.TOOL_HELI) {
                if (model.curSpriteX >= 0) {
                    g2d.drawImage(model.heliImage, model.curSpriteX, model.curSpriteY, null);
                }
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Container parent = getParent();
        if (parent instanceof JViewport) {
            Dimension containerDimension = ((JViewport)parent).getSize();
            if (containerDimension.width > maxDimension.width)
                curDimension.width = containerDimension.width;
            else
                curDimension.width = maxDimension.width;
            if (containerDimension.height > maxDimension.height)
                curDimension.height = containerDimension.height;
            else
                curDimension.height = maxDimension.height;
        }
        return curDimension;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL)
            return 10;
        else
            return visibleRect.height - 480;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL)
            return visibleRect.width;
        else
            return visibleRect.height - model.maxHeight;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
