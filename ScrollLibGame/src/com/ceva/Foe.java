package com.ceva;

public class Foe {
    // Atributos de los enemigos
    public static final int FOE_Y_MASK          = 0x00000fff;   // primeros 12 bits = Posicion Y.

    public static final int FOE_TYPE_SHIFT_MASK = 12;
    public static final int FOE_TYPE_MASK       = 0x000ff000;   // siguientes 8 bits == Tipo de enemigo
    public static final int FOE_TYPE_HELI       = 1;

    public static final int FOE_AI_SHIFT_MASK   = 20;
    public static final int FOE_AI_MASK         = 0x00f00000;   // siguientes 4 bits = Tipo de AI del enemigo.
    public static final int FOE_AI_STRAIGHT     = 0;
    public static final int FOE_AI_YFOLLOW      = 1;
    public static final int FOE_AI_YWAVE        = 2;

    public static final int FOE_STRENGTH_SHIFT_MASK = 24;
    public static final int FOE_STRENGTH_MASK   = 0x0f000000;   // siguientes 4 bits = Fuerza del enemigo (16 niveles).

    public static final int FOE_DISABLEDFIRE_SHIFT_MASK = 28;
    public static final int FOE_DISABLEDFIRE_MASK = 0x10000000; // siguiente 1 bit = Disparos deshabilitados.

    // informacion para describir a un enemigo
    // x, y son coordenadas iniciale del enemigo
    public int x;
    public int y;
    public int type; // el juego da la posibilidad de crear mas tipos de enemigos, como tanques bombas etc
    public int ai; // inteligencia artificial
    public int health; // cuantas balas tendra que recibir para ser destruido
    public boolean disabledFire;

    public Foe next;

    public Foe() {
    }

    public Foe(int x, int y, int type, int ai) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.ai = ai;
    }

    public static int getStrengthLevel(int health) {
        int level = 0;
        while (health != 0) {
            health = health >>> 1;
            level++;
        }
        return level;
    }

    public int strengthLevel() {
        int h = health;
        int level = 0;
        while (h > 0) {
            h = h >>> 1;
            level++;
        }
        return level;
    }

    public static Foe addFoe(Foe foe, Foe root) {
        Foe ins;
        if (root == null) {
            foe.next = null;
            return foe;
        } else {
            ins = root;
            Foe prev = null;
            while ((ins.next != null) && (ins.x < foe.x)) {
                prev = ins;
                ins = ins.next;
            }
            if (ins.x < foe.x) {
                foe.next = ins.next;
                ins.next = foe;
            } else {
                if (prev == null) {
                    foe.next = ins;
                    root = foe;
                } else {
                    prev.next = foe;
                    foe.next = ins;
                }
            }
        }
        return root;
    }
}
