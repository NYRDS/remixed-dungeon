package android.view;

import com.badlogic.gdx.Input;

public class KeyEvent {
    public static final int KEYCODE_BACK = Input.Keys.ESCAPE;
    public static final int KEYCODE_MENU = 2;

    public static final int KEYCODE_DPAD_UP = Input.Keys.UP;
    public static final int KEYCODE_DPAD_DOWN = Input.Keys.DOWN;
    public static final int KEYCODE_DPAD_LEFT = Input.Keys.LEFT;
    public static final int KEYCODE_DPAD_RIGHT = Input.Keys.RIGHT;
    public static final int KEYCODE_SPACE = Input.Keys.SPACE;

    public static final int KEYCODE_0 = Input.Keys.NUM_0;
    public static final int KEYCODE_1 = Input.Keys.NUM_1;
    public static final int KEYCODE_2 = Input.Keys.NUM_2;
    public static final int KEYCODE_3 = Input.Keys.NUM_3;
    public static final int KEYCODE_4 = Input.Keys.NUM_4;
    public static final int KEYCODE_5 = Input.Keys.NUM_5;
    public static final int KEYCODE_6 = Input.Keys.NUM_6;
    public static final int KEYCODE_7 = Input.Keys.NUM_7;
    public static final int KEYCODE_8 = Input.Keys.NUM_8;
    public static final int KEYCODE_9 = Input.Keys.NUM_9;
    
    public static final int KEYCODE_NUMPAD_0 = Input.Keys.NUMPAD_0;
    public static final int KEYCODE_NUMPAD_1 = Input.Keys.NUMPAD_1;
    public static final int KEYCODE_NUMPAD_2 = Input.Keys.NUMPAD_2;
    public static final int KEYCODE_NUMPAD_3 = Input.Keys.NUMPAD_3;
    public static final int KEYCODE_NUMPAD_4 = Input.Keys.NUMPAD_4;
    public static final int KEYCODE_NUMPAD_5 = Input.Keys.NUMPAD_5;
    public static final int KEYCODE_NUMPAD_6 = Input.Keys.NUMPAD_6;
    public static final int KEYCODE_NUMPAD_7 = Input.Keys.NUMPAD_7;
    public static final int KEYCODE_NUMPAD_8 = Input.Keys.NUMPAD_8;
    public static final int KEYCODE_NUMPAD_9 = Input.Keys.NUMPAD_9;

    public static final int KEYCODE_DPAD_DOWN_LEFT = Input.Keys.END;
    public static final int KEYCODE_DPAD_DOWN_RIGHT = Input.Keys.PAGE_DOWN;
    public static final int KEYCODE_DPAD_UP_LEFT = Input.Keys.HOME;
    public static final int KEYCODE_DPAD_UP_RIGHT = Input.Keys.PAGE_UP;


    public static final int KEYCODE_I = Input.Keys.I;
    public static final int KEYCODE_E = Input.Keys.E;
    public static final int KEYCODE_S = Input.Keys.S;

    public static final int KEYCODE_VOLUME_UP = 3;
    public static final int KEYCODE_VOLUME_DOWN = 4;
    public static final int ACTION_DOWN = 5;
    public static final int ACTION_UP = 6;

    private final int code;
    private final int action;

    public KeyEvent(int keyCode, int action) {
        code = keyCode;
        this.action = action;
    }
    public int getAction() {
        return action;
    }

    public int getKeyCode() {
        return code;
    }
}
