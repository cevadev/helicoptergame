package com.ceva;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class KeyBoard {
    // estados de la tecla
    public static final int KBD_RELEASED = 0; // La tecla no esta presionada.
    public static final int KBD_PRESSED  = 1; // La tecla se encuentra presionada.
    public static final int KBD_IGNORED  = 2; // Decidi ignorar el estado que esta tecla tenga (hasta que sea liberada).

    // Map que registra las teclas presionadas key = tecla, value = estado
    private JPanel ownerPanel;
    private final Map<Integer,Integer> keyCodeMap = new HashMap<>(); // key=KeyCode, value=state
    // map que maneja eventos de la tecla presionada. llave es la tecla y el value un objeto KeyEventNode
    private final Map<Integer,KeyEventNode> keyPressedCallbackMap = new HashMap<>();
    // map que maneja eventos de la tecla liberada
    private final Map<Integer,KeyEventNode> keyReleasedCallbackMap = new HashMap<>();
    KeyboardKeyAdapter keyListener;

    public KeyBoard(JPanel ownerPanel) {
        this.ownerPanel = ownerPanel;
        keyListener = new KeyboardKeyAdapter();
        ownerPanel.addKeyListener(keyListener);
    }

    /**
     * Cuando se produce un evento, el metodo keyEvent sera invocado
     */
    private class KeyEventNode {
        IKeyCallback callback; // permite definir el metodo a invocar
        KeyEventNode next; // referencia al sgte nodo
    }

    /**
     * Devuelve true si la tecla keyCode esta siendo presionada.
     *
     * @param keyCode KeyEvent key code.
     * @return
     */
    public boolean keyPressed(int keyCode) {
        int code = keyCodeMap.getOrDefault(keyCode, KBD_RELEASED);
        // preguntamos si esta encendido el bit KDB_PRESSED y la tecla no esta ignorada entonces true
        return ((code & KBD_PRESSED) != 0) && ((code & KBD_IGNORED) == 0);
    }

    public int keyStatus(int keyCode) {
        return keyCodeMap.getOrDefault(keyCode, KBD_RELEASED);
    }

    /**
     * Cambia el estatus de una tecla, para que no se vuelva a generar el evento mientras
     * siga presionada.
     *
     * @param keyCode
     */
    public void ignoreKey(int keyCode) {
        int code = keyCodeMap.getOrDefault(keyCode, KBD_RELEASED);
        keyCodeMap.put(keyCode, code | KBD_IGNORED);
    }

    public void close() {
//        ownerPanel.removeKeyListener(keyListener);
    }

    /**
     * Metodo que agrega un nuevo listener
     * @param code: recibe codigo de la tecla
     * @param callback: funcion callback
     */
    public void addKeyEventNode(Map<Integer,KeyEventNode> map,int code, IKeyCallback callback){
        // obtenemos nodo de la tecla que esta en el mapa
        KeyEventNode node = map.get(code);;
        if (node == null) {
            // no existe el node, creamos uno
            node = new KeyEventNode();
            node.callback = callback;
            // guardamos el nodo en la coleccion
            map.put(code, node);
        } else {
            // existe el nodo, entonce recorremos la lista enlazada hasta el final
            while (node.next != null)
                node = node.next;
            // agregamos el nuevo callback
            KeyEventNode newNode = new KeyEventNode();
            newNode.callback = callback;
            node.next = newNode;
        }
    }

    /**
     * Metodo para quitar un listener de la lista
     * @param code
     * @param callback
     */
    public void removeKeyEventNode(Map<Integer,KeyEventNode> map, int code, IKeyCallback callback){
        KeyEventNode node = map.get(code);
        // si existe un nodo en el map, comenzamos a recorrerlo
        if (node != null) {
            KeyEventNode prev = null;
            while ((node != null) && (node.callback != callback)) {
                // guardamos una referencia al nodo anterior
                prev = node;
                node = node.next;
            }
            if (node != null) {
                if (prev != null)
                    // unimos el campo next del nodo anterior con el campo next del nodo a eliminar
                    prev.next = node.next;
                else {
                    node = node.next;
                    map.put(code, node);
                }
            }
        }
    }

    public void addKeyPressedListener(int code, IKeyCallback callback) {
        addKeyEventNode(keyPressedCallbackMap, code, callback);
    }

    public void removeKeyPressedListener(int code, IKeyCallback callback) {
        removeKeyEventNode(keyPressedCallbackMap, code, callback);
    }

    public void addKeyReleasedListener(int code, IKeyCallback callback) {
        addKeyEventNode(keyReleasedCallbackMap, code, callback);
    }

    public void removeKeyReleasedListener(int code, IKeyCallback callback) {
        removeKeyEventNode(keyReleasedCallbackMap, code, callback);
    }

    /**
     * Clase que permite manejar los eventos del teclado
     */
    private class KeyboardKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            // obtenemos la tecja presionada
            int keyCode = e.getKeyCode();
            // obtenemos el estado
            int state = keyCodeMap.getOrDefault(keyCode, 0);
            if ((state & KBD_PRESSED) != 0)
                return; // No hay cambio de estado
            // encendemos el bit KBD_PRESSED
            state |= KBD_PRESSED;
            // Guardamoe el estado en el mapa de las teclas
            keyCodeMap.put(keyCode, state);

            // obtenemos el posible nodo de un listener que exista para la tecla
            KeyEventNode node = keyPressedCallbackMap.get(keyCode);
            while (node != null) {
                // recorremos cada de la lista enlazada e invocamos callback
                node.callback.keyEvent(e);
                node = node.next;
            }
        }

        // metodo cuando una tecla ha sido liberada
        @Override
        public void keyReleased(KeyEvent e) {
            // get keycode
            int keyCode = e.getKeyCode();
            // guardamos la tecla como liberada
            keyCodeMap.put(keyCode, KBD_RELEASED);

            // obtenemos el nodo para los listeners de tecla liberada
            KeyEventNode node = keyReleasedCallbackMap.get(keyCode);
            while (node != null) {
                // invocamos el callback de cada nodo en la lista enlazada
                node.callback.keyEvent(e);
                node = node.next;
            }
        }
    }

}
