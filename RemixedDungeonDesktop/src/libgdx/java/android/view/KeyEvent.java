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
