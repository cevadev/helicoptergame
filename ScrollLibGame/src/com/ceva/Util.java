package com.ceva;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {
    public static String defaultClassPath = null;
    // generamos un nuevo aleatorio entre 0 y 32767
    public static int rand(){
        return (int)(Math.random() * 327680);
    }

    // retornamos numero aleatorio dentro de un rango de numeros (pot ejemplo entre 50 y 100)
    public static int rand_range(int x, int y){
        return (x + (rand() % (y - x + 1)));
    }

    /*
     * convertimos el contenido del int que recibimos como parametro a 12 bits
     */
    public static int intTo12Bit(int n) {
        n = n & 0xfff;
        return n;
    }

    /*
     * el bit 12 determina el signo del numero
     */
    public static int cvt12BitToInt(int n) {
        // validamos si se trata d un numero negativo de 12 bits
        if ((n & 0x800) != 0) {
            n = (~n + 1) & 0xfff;
            return -n;
        }
        n = n & 0xfff;
        return n;
    }

    /*
    Open a file and return an InputStream.

    - First, the file is searched as an absolute (or relative path). (c:\myfile.txt).
    - If the file is not found, then it is search as the filename inside user.dir ({user.dir}/myFile.txt).
    - If the file is not found, then it is search with getResourceAsStream (com/rcosio/myfile.txt).
    */
    public static InputStream openStream(String filename, String defaultPath) {
        // primero intentamos abrir el archivo tal cual (ruta absoluta/relativa)
        File file = new File(filename);
        if (!file.exists()) {
            // Try as a file in user.dir
            file = new File(System.getProperty("user.dir"), file.getName());
        }
        try {
            if (file.exists())
                return new FileInputStream(file);
            else {
                // Finally, try as classpath. asumimos q se encuentra en el package default
                InputStream in = Util.class.getResourceAsStream((defaultPath == null ? "/" : defaultPath + "/") + filename);
                if (in == null) {
                    System.out.println("Error: resource not found: " + ((defaultPath == null ? "/" : defaultPath) + filename));
                }
                return in;
            }
        } catch (IOException e) {
            System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean findResource(String name, String defaultPath) {
        File file = new File(name);
        if (file.exists())
            return true;
        file = new File(System.getProperty("user.dir"), file.getName());
        if (file.exists())
            return true;
        if (Util.class.getResource((defaultPath == null ? "/" : defaultPath + "/") + name) != null)
            return true;
        return false;
    }

    /**
     * Metodo que lee una imagen del disco duro
     * @param fileName
     * @param defaultPath
     * @return
     */
    public static BufferedImage readImageDirect(String fileName, String defaultPath) {
        BufferedImage result = null;
        try (InputStream in = openStream(fileName, defaultPath)) {
            if (in != null) {
                result = ImageIO.read(in);
            }
        } catch (IOException e) {
            System.out.println(e.getClass().getName() + " generated: " + e.getMessage());
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    /**
     * metodo que lee la imagen .png y la carga como un BufferedImage[]
     * @param fileName
     * @param size - tamano de cada cuadro en px. La imagen helicopter tiene width 256.
     *             dividimos 256/64 = 4, que son las 4 imagenes del archivo hilicopter.png
     * @return
     */
    public static BufferedImage[] loadFrameSequence(String fileName, int size) {
        BufferedImage tmp = readImageDirect(fileName, defaultClassPath);
        // width de la imagen es 256, size = 64px, crea un array de 4 imagenes
        BufferedImage result[] = new BufferedImage[tmp.getWidth()/size];
        for (int n=0; (n*size)<tmp.getWidth(); n++) {
            result[n] = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = result[n].createGraphics();
            // dibujamos el grafico result[n] en la coordenada
            g2d.drawImage(tmp, -n*size, 0, null);
            g2d.dispose();
        }
        return result;
    }

}
