package com.ceva;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
public class ScrollLevel {
    private static final int DATSIGNATURE = 0x52435601;
    public static final int LEVELTYPE_FLAT = 1;
    public static final int LEVELTYPE_NORMAL = 2;

    public int numLevel;
    public int levelType;
    public int width;
    public short levelH[]; // representa numero de pixeles hacia arriba de la pantalla
    public short levelL[]; // representa numero de pixeles hacia abajo de la pantalla
    /*
     * contien la pos inicial desde donde se dibujara en la pantalla
     * al momento de dibujar comienza por la izquierda
     */
    public int curXLevel;
    public ScrollLevel next; // representa el sgte nivel a dibujar
    public Foe foeList; // lista de enemigos que apareceran en el juego
    public Foe curFoePtr;
    private Screen screen;
    private String levelMessage;

    public ScrollLevel(Screen screen) {
        this.screen = screen;
    }

    public static ScrollLevel createPlainLevel(Screen screen, int width, int numLevel) {
        ScrollLevel lvl = new ScrollLevel(screen);
        lvl.numLevel = numLevel;
        lvl.width = width;
        lvl.levelL = new short[width];
        lvl.levelH = new short[width];
        for (int n=0; n<lvl.levelL.length; n++) {
            lvl.levelH[n] = 10;
            lvl.levelL[n] = (short)(screen.getHeight() -10);
            if ((n % 15) == 0) {
                lvl.levelH[n] = 5;
                lvl.levelL[n] = (short)(screen.getHeight() - 5);
            }
        }
        lvl.levelType = LEVELTYPE_FLAT;
        lvl.levelMessage = String.format("Level %02d", numLevel);
        return lvl;
    }

    /**
     * Interpreta el contenido del archivo de tipo binario para generar un nivel
     * @param in
     * @throws IOException
     */
    public void load(InputStream in) throws IOException {
        // Usamos DataInputStream para leer el archivo de niveles de tipo binario
        try (DataInputStream din = new DataInputStream(in)) {
            // el primer dato en el archivo corresponde a la firma
            int n = din.readInt();
            // validamos que la firma sea la correcta
            if (n != DATSIGNATURE) {
                System.out.println("Error loading level: Invalid signature.");
                return;
            }
            // el sgte byte del archivo contiene el numero de version
            n = din.readByte();
            // validamos que la version sea la correcta
            if (n != 1) {
                System.out.println("Error loading level: Unsupported version.");
                return;
            }
            // el sgte byte corresponde al tamano de los arreglos levelL y levelH (niveles)
            n = din.readInt();
            System.out.println("Level size is: " + (n/Screen.FPS) + " secs.");
            levelH = new short[n];
            for (n=0; n<levelH.length; n++)
                levelH[n] = din.readShort();
            n = din.readInt();
            levelL = new short[n];
            for (n=0; n<levelL.length; n++)
                levelL[n] = din.readShort();
            width = n;

            n = din.readInt(); // foe size
            while (n > 0) {
                Foe f = new Foe();
                f.x = din.readShort() & 0xffff;
                int i = din.readInt();
                f.y = Util.cvt12BitToInt(i);
                f.type = (i & Foe.FOE_TYPE_MASK) >>> Foe.FOE_TYPE_SHIFT_MASK;
                f.ai = (i & Foe.FOE_AI_MASK) >>> Foe.FOE_AI_SHIFT_MASK;
                int health = (i & Foe.FOE_STRENGTH_MASK) >>> Foe.FOE_STRENGTH_SHIFT_MASK;
                f.health = (1 << health) - 1;
                f.disabledFire = ((i & Foe.FOE_DISABLEDFIRE_MASK) >>> Foe.FOE_DISABLEDFIRE_SHIFT_MASK) != 0;
                foeList = Foe.addFoe(f, foeList);
                n--;
            }

            curFoePtr = foeList;
            levelType = LEVELTYPE_NORMAL;
        }
    }

    /**
     * Cargamos el nivel de acuerdo a un numero de nivel
     * @param nLevel
     * @return
     */
    public ScrollLevel loadLevel(int nLevel) {
        try {
            ScrollLevel lvl = new ScrollLevel(screen);
            InputStream in = Util.openStream(String.format("level%02d.dat", nLevel), Util.defaultClassPath);
            if (in == null) {
                nLevel = 1;
                in = Util.openStream("level01.dat", Util.defaultClassPath);
            }
            try {
                lvl = new ScrollLevel(screen);
                lvl.load(in);
                lvl.numLevel = nLevel;
            } finally {
                in.close();
            }
            return lvl;
        } catch (IOException e) {
            System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Metodo que sera invocado una y otra vez desde la aplicacion principal para generar el movieminto
     * horizontal del terreno
     * @return objecto ScrollLevel ya que el juego principal debe tener referencia al nivel actual
     * cuando hay un cambio de nivel la funcion retorna el sgte ScrollLevel que se convertirá en
     * el nuevo nivel actual
     */
    public ScrollLevel scroll(int nx) {
        curXLevel = curXLevel + nx;
        if (((curXLevel + screen.getWidth()) >= width) && (next == null)) {
            // Cargar siguiente nivel
            System.out.println("load next level...");
            ScrollLevel lvl;
            if (levelType == LEVELTYPE_FLAT)
                lvl = loadLevel(numLevel);
            else
                lvl = loadLevel(numLevel + 1);
            next = lvl;
            if (levelType != LEVELTYPE_FLAT) {
                lvl = createPlainLevel(screen, screen.getWidth()*3/2, next.numLevel);
                lvl.next = next;
                next = lvl;
            }
        }
        if (curXLevel >= width) {
            // Comenzar siguiente nivel
            curXLevel -= width;
            return next;
        }
        return this;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        ScrollLevel lvl = this;
        int n = lvl.curXLevel;
        // iteramos desde 0 hasta el ancho de la pantalla
        for (int i=0; i<screen.getWidth(); i++) {
            // dibujamos la linea inferior
            short s = lvl.levelL[n];
            g2d.drawLine(i, s, i, screen.getHeight());

            // dibujamos la linea superior
            s = lvl.levelH[n];
            g2d.drawLine(i, 0, i, s);

            n = (n + 1);
            if (n >= lvl.levelL.length) {
                n -= lvl.levelL.length;
                lvl = lvl.next;
            }
        }
        // dibujamos el nombre del nivel
        if ((levelType == LEVELTYPE_FLAT) && (levelMessage != null)) {
            int xMsg = width - (screen.getWidth()*3/2);
            if (curXLevel >= xMsg) {
                g2d.drawString(levelMessage, screen.getWidth()-(curXLevel-xMsg), screen.getHeight()/2);
            }
        }
    }
}
