package android.view;

// headless: self-consistent key codes, mirrors the desktop shim's structure
// without the libgdx Input.Keys dependency
public class KeyEvent {
	public static final int KEYCODE_BACK = 131;
	public static final int KEYCODE_MENU = 2;

	public static final int KEYCODE_DPAD_UP = 19;
	public static final int KEYCODE_DPAD_DOWN = 20;
	public static final int KEYCODE_DPAD_LEFT = 21;
	public static final int KEYCODE_DPAD_RIGHT = 22;
	public static final int KEYCODE_SPACE = 62;

	public static final int KEYCODE_0 = 7;
	public static final int KEYCODE_1 = 8;
	public static final int KEYCODE_2 = 9;
	public static final int KEYCODE_3 = 10;
	public static final int KEYCODE_4 = 11;
	public static final int KEYCODE_5 = 12;
	public static final int KEYCODE_6 = 13;
	public static final int KEYCODE_7 = 14;
	public static final int KEYCODE_8 = 15;
	public static final int KEYCODE_9 = 16;

	public static final int KEYCODE_NUMPAD_0 = 144;
	public static final int KEYCODE_NUMPAD_1 = 145;
	public static final int KEYCODE_NUMPAD_2 = 146;
	public static final int KEYCODE_NUMPAD_3 = 147;
	public static final int KEYCODE_NUMPAD_4 = 148;
	public static final int KEYCODE_NUMPAD_5 = 149;
	public static final int KEYCODE_NUMPAD_6 = 150;
	public static final int KEYCODE_NUMPAD_7 = 151;
	public static final int KEYCODE_NUMPAD_8 = 152;
	public static final int KEYCODE_NUMPAD_9 = 153;

	public static final int KEYCODE_DPAD_DOWN_LEFT = 132;
	public static final int KEYCODE_DPAD_DOWN_RIGHT = 135;
	public static final int KEYCODE_DPAD_UP_LEFT = 90;
	public static final int KEYCODE_DPAD_UP_RIGHT = 92;

	public static final int KEYCODE_I = 37;
	public static final int KEYCODE_E = 33;
	public static final int KEYCODE_S = 40;

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
