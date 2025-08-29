package com.nyrds.platform.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.watabou.utils.Signal;

/**
 * HTML version of Keys class
 */
public class Keys {
    
    public static final int BACK = Input.Keys.ESCAPE;
    public static final int MENU = Input.Keys.MENU;
    public static final int VOLUME_UP = Input.Keys.PLUS;
    public static final int VOLUME_DOWN = Input.Keys.MINUS;
    
    // Numeric keys
    public static final int NUM_0 = Input.Keys.NUM_0;
    public static final int NUM_1 = Input.Keys.NUM_1;
    public static final int NUM_2 = Input.Keys.NUM_2;
    public static final int NUM_3 = Input.Keys.NUM_3;
    public static final int NUM_4 = Input.Keys.NUM_4;
    public static final int NUM_5 = Input.Keys.NUM_5;
    public static final int NUM_6 = Input.Keys.NUM_6;
    public static final int NUM_7 = Input.Keys.NUM_7;
    public static final int NUM_8 = Input.Keys.NUM_8;
    public static final int NUM_9 = Input.Keys.NUM_9;
    
    public static final Signal<Key> event = new Signal<>(true);
    
    public static String getKeyLabel(int keyCode) {
        // Map common keys to their display labels
        switch (keyCode) {
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                return "Shift";
            case Input.Keys.CONTROL_LEFT:
            case Input.Keys.CONTROL_RIGHT:
                return "Ctrl";
            case Input.Keys.ALT_LEFT:
            case Input.Keys.ALT_RIGHT:
                return "Alt";
            case Input.Keys.SPACE:
                return "Space";
            case Input.Keys.ENTER:
                return "Enter";
            case Input.Keys.TAB:
                return "Tab";
            case Input.Keys.ESCAPE:
                return "Esc";
            case Input.Keys.BACKSPACE:
                return "Backspace";
            case Input.Keys.UP:
                return "↑";
            case Input.Keys.DOWN:
                return "↓";
            case Input.Keys.LEFT:
                return "←";
            case Input.Keys.RIGHT:
                return "→";
            default:
                return Character.toString((char) keyCode);
        }
    }
    
    public static class Key {
        public int code;
        public boolean pressed;
        
        public Key(int code, boolean pressed) {
            this.code = code;
            this.pressed = pressed;
        }
    }
}