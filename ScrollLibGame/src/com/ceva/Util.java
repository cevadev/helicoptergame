package com.ceva;

public class Util {
    // generamos un nuevo aleatorio entre 0 y 32767
    public static int rand(){
        return (int)(Math.random() * 327680);
    }

    // retornamos numero aleatorio dentro de un rango de numeros (pot ejemplo entre 50 y 100)
    public static int rand_range(int x, int y){
        return (x + (rand() % (y - x + 1)));
    }
}
