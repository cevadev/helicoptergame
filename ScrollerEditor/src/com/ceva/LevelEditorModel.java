package com.ceva;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
public class LevelEditorModel {
    public static final int TOOL_NONE   = 0;
    public static final int TOOL_LINE   = 1;
    public static final int TOOL_FREE   = 2;
    public static final int TOOL_HELI   = 3;

    // coleccion de observers
    private Set<ModelObserver> observers;
    // La coordenada Y del terreno superior.
    public short levelH[];
    // La coordenada Y del terreno inferior.
    public short levelL[];

    // Informacion para la edicion de terreno
    public short tmpLevelData[];
    public int tmpLevelDataLen;
    public int tmpLevelDataX;
    public boolean upOrDown;

    public Foe foes = null;           // Lista de enemigos
    public Foe highlightedFoe = null; // Enemigo resaltado
    public Foe selectedFoe = null;    // Enemigo seleccionado
    public int currentTool;
    public int curSpriteX;
    public int curSpriteY;
    public final int maxHeight = 480;
    protected boolean dirty;
    protected File currentFile = null;

    public BufferedImage heliImage;

    public LevelEditorModel() {
        observers = new HashSet<>();
        try (InputStream in = Util.openStream("foe-heli.png", Util.defaultClassPath)) {
            if (in == null)
                throw new RuntimeException("Can't read foe-heli.png");
            BufferedImage img = ImageIO.read(in);
            heliImage = new BufferedImage(64, 64, img.getType());
            Graphics2D g2d = (Graphics2D) heliImage.getGraphics();
            g2d.drawImage(img, 0, 0, null);
            g2d.dispose();
        } catch (IOException e) {
            System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void notifyRepaint() {
        for (ModelObserver observer : observers)
            observer.repaint();
    }

    private void notifyMaxDimension(int width) {
        for (ModelObserver observer : observers)
            observer.setMaxDimension(width, maxHeight);
    }

    public void setDirty() {
        dirty = true;
        notifyRepaint();
    }

    public void setSelectedFoe(Foe foe) {
        selectedFoe = foe;
        notifyRepaint();
    }

    public void setHighlightedFoe(Foe foe) {
        highlightedFoe = foe;
        notifyRepaint();
    }

    public void reOrderFoe(Foe foe) {
        Foe prev = null;
        Foe f = foes;
        if (f != foe) {
            while ((f != null) && (f != foe)) {
                prev = f;
                f = f.next;
            }
        }
        if (f == foe) {
            if (prev != null) {
                prev.next = f.next;
            } else {
                foes = foes.next;
            }
            f.next = null;
            foes = Foe.addFoe(f, foes);
        }
    }

    public void removeFoe(Foe foe) {
        Foe prev = null;
        Foe f = foes;
        if (f != foe) {
            while ((f != null) && (f != foe)) {
                prev = f;
                f = f.next;
            }
        }
        if (f == foe) {
            if (prev != null) {
                prev.next = f.next;
            } else {
                foes = foes.next;
            }
        }

        if (foe == selectedFoe)
            selectedFoe = null;
        if (foe == highlightedFoe)
            highlightedFoe = null;
        setDirty();

        f = foes;
        while (f != null) {
            System.out.printf(" x:%d", f.x);
            f = f.next;
        }
        System.out.println();
    }

    public void updateFoe(Foe foe, int ai, int health, boolean disabledFire) {
        foe.ai = ai;
        foe.health = health;
        foe.disabledFire = disabledFire;
        dirty = true;
    }

    private boolean ptInRect(int x, int y, int rectX, int rectY, int rectWidth, int rectHeight) {
        if ((x >= rectX) && (x <= (rectX+rectWidth)) &&
                (y >= rectY) && (y <= (rectY+rectHeight)))
            return true;
        return false;
    }

    public Foe findFoe(int x, int y) {
        Foe f = foes;
        while (f != null) {
            if (ptInRect(x, y, f.x, f.y, 64, 64)) {
                return f;
            }
            f = f.next;
        }
        return null;
    }

    public void createLevel(int sizeInSecs) {
        levelH = new short[sizeInSecs*60];
        levelL = new short[levelH.length];

        int n = 0;
        short curY = (short) (maxHeight-1);
        while (n < levelL.length) {
            curY += Util.rand_range(-5, 5);
            if (curY < 0)
                curY = 0;
            else if (curY > maxHeight)
                curY = (short) maxHeight;

            levelL[n++] = curY;
        }

        n = 0;
        curY = 0;
        while (n < levelH.length) {
            curY += Util.rand_range(-5, 5);

            if ((curY + (maxHeight - levelL[n]) + 60) >= maxHeight)
                curY = (short) (levelL[n] - 60);

            if (curY < 0)
                curY = 0;
            else if (curY > maxHeight)
                curY = (short) maxHeight;

            levelH[n++] = curY;
        }

        foes = null;

        notifyMaxDimension(levelH.length);
        currentFile = null;
        dirty = true;
    }

    /*
    Header
        int                   : identifier.
        byte                  : version.
        int                   : levelH size.
        short * levelH.size   : LevelH elements.
        int                   : LevelL size.
        short * levelL.size   : LevelL elements.
        int                   : foe size.
        (short+int)*foes.size : foe elements.
    */
    public void save(File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            DataOutputStream dout = new DataOutputStream(out);

            dout.writeInt(0x52435601);  // RCV01
            dout.writeByte(0x01);       // Version
            dout.writeInt(levelH.length);
            for (short s : levelH) {
                dout.writeShort(s);
            }
            dout.writeInt(levelL.length);
            for (short s : levelL) {
                dout.writeShort(s);
            }
            int n = 0;
            Foe f = foes;
            while (f != null) {
                n++;
                f = f.next;
            }
            dout.writeInt(n);
            int nSaved = 0;
            f = foes;
            while (f != null) {
                n = f.x & 0xffff;
                dout.writeShort(n);
                n = Util.intTo12Bit(f.y) +
                        ((f.type << Foe.FOE_TYPE_SHIFT_MASK) & Foe.FOE_TYPE_MASK) +
                        ((f.ai << Foe.FOE_AI_SHIFT_MASK) & Foe.FOE_AI_MASK);
                if (f.disabledFire)
                    n |= (1 << Foe.FOE_DISABLEDFIRE_SHIFT_MASK);
                int healthLevel = Foe.getStrengthLevel(f.health);
                n = n | ((healthLevel << Foe.FOE_STRENGTH_SHIFT_MASK) & Foe.FOE_STRENGTH_MASK);

                dout.writeInt(n);
                f = f.next;
                nSaved++;
            }
            System.out.printf("%d foes saved.\n", nSaved);
            dout.close();
            dirty = false;
            currentFile = file;
        }
    }

    /*
    Header
        int                   : identifier.
        byte                  : version.
        int                   : levelH size.
        short * levelH.size   : LevelH elements.
        int                   : LevelL size.
        short * levelL.size   : LevelL elements.
        int                   : foe size.
        (short+int)*foes.size : foe elements.
    */
    public void load(File file) throws IOException {
        foes = null;
        selectedFoe = null;
        highlightedFoe = null;

        try (InputStream in = new FileInputStream(file); DataInputStream din = new DataInputStream(in)) {
            int n = din.readInt();
            if (n != 0x52435601) {     // RCV01
                System.out.println("Error loading file: Invalid file.");
                return;
            }
            n = din.readByte();             // Version
            if (n != 1) {
                System.out.println("Error loading file: Unsupported verison.");
                return;
            }
            n = din.readInt();
            levelH = new short[n];
            for (n=0; n<levelH.length; n++) {
                levelH[n] = din.readShort();
            }
            n = din.readInt();
            levelL = new short[n];
            for (n=0; n<levelL.length; n++) {
                levelL[n] = din.readShort();
            }

            n = din.readInt(); // foe size
            int nLeft = n;
            while (nLeft > 0) {
                Foe f = new Foe();
                int i;
                try {
                    f.x = din.readShort() & 0xffff;
                    i = din.readInt();
                } catch (IOException e) {
                    System.out.println("Warninig: EOF found loading file.");
                    break;
                }
                f.y = Util.cvt12BitToInt(i);
                if (f.y > maxHeight)
                    f.y = 0;
                f.type = (i & Foe.FOE_TYPE_MASK) >>> Foe.FOE_TYPE_SHIFT_MASK;
                f.ai = (i & Foe.FOE_AI_MASK) >>> Foe.FOE_AI_SHIFT_MASK;
                int healthLevel = (i & Foe.FOE_STRENGTH_MASK) >>> Foe.FOE_STRENGTH_SHIFT_MASK;
                f.health = (1 << healthLevel) - 1;
                f.disabledFire = (i & Foe.FOE_DISABLEDFIRE_MASK) != 0;
                foes = Foe.addFoe(f, foes);
                nLeft--;
            }
            System.out.printf("%d foes loaded.\n", (n-nLeft));
        }
        dirty = false;
        currentFile = file;

        notifyMaxDimension(levelH.length);
    }

    public void addFoe(int x, int y) {
        Foe foe = new Foe(x, y, Foe.FOE_TYPE_HELI, Foe.FOE_AI_STRAIGHT);
        foes = Foe.addFoe(foe, foes);
        setDirty();
    }

    public void writeTmpLevelData() {
        // copy data to model
        if (tmpLevelDataLen == 0)
            return;
        if (tmpLevelDataX < 0)
            tmpLevelDataX = 0;
        short[] dest = upOrDown ? levelH : levelL;
        int size = tmpLevelDataLen;
        if ((size + tmpLevelDataX) >= dest.length)
            size = dest.length - tmpLevelDataX;
        int x = tmpLevelDataX;
        for (int n=0; n<size; n++)
            dest[x++] = tmpLevelData[n];
        setDirty();
    }

    public void addObserver(ModelObserver observer) {
        observers.add(observer);
        if (levelH != null)
            observer.setMaxDimension(levelH.length, maxHeight);
        else
            observer.setMaxDimension(0, maxHeight);
    }

    public void removeObserver(ModelObserver observer) {
        observers.remove(observer);
    }
}
