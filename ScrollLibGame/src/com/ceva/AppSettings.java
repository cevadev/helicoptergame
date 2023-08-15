package com.ceva;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/*
 * Clase que nos permite guardar el ultimo archivo que se leyo en el editor de niveles
 */
public class AppSettings {
    private static AppSettings instance;
    private Properties properties;
    private static final String USER_HOME = System.getProperty("user.dir");

    private AppSettings(){}

    // guardar y cargar las opciones
    public AppSettings setString(String name, String value){
        properties.setProperty(name, value);
        return this;
    }

    public String getString(String name){
        return properties.getProperty(name);
    }

    public int getInt(String name){
        String s = properties.getProperty(name);
        if((s == null) || (s.length() == 0)){
            return 0;
        }
        try{
            return Integer.parseInt(s);
        }
        catch(NumberFormatException ex){
            return 0;
        }
    }

    public AppSettings setInt(String name, int value){
        properties.setProperty(name, String.valueOf(value));
        return this;
    }

    public void load(){
        properties = new Properties();
        // obtenemos el directorio del usuario
        File userDir = new File(USER_HOME);
        File propsFile = new File(userDir, "com.ceva.settings.xml");
        // validamos que exista el archivo y que sea un archivo valido
        if(propsFile.exists() && propsFile.isFile()){
            try(InputStream in = new FileInputStream(propsFile)) {
                properties.loadFromXML(in);
            }
            catch(IOException ex){
                System.out.println(ex.getClass().getName() + " generated: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public void save(){
        File strUserDir = new File(USER_HOME);
        File propsFile = new File(strUserDir, "com.ceva.settings.xml");
        try(OutputStream out = new FileOutputStream(propsFile)){
            properties.storeToXML(out, "com.ceva.settings file", StandardCharsets.UTF_8);
        }
        catch(IOException ex){}
    }

    public static AppSettings getInstance(){
        // la 1era vez, instance sera null
        if(instance != null){
            return instance;
        }
        instance = new AppSettings();
        instance.load();
        return instance;
    }
}
