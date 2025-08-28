package com.nyrds.platform.input;

import android.view.KeyEvent;

import com.watabou.utils.Signal;

public class Keys {
	
	public static final int BACK		= KeyEvent.KEYCODE_BACK;
	public static final int MENU		= KeyEvent.KEYCODE_MENU;
	public static final int VOLUME_UP	= KeyEvent.KEYCODE_VOLUME_UP;
	public static final int VOLUME_DOWN	= KeyEvent.KEYCODE_VOLUME_DOWN;
	
	// Numeric keys
	public static final int NUM_0		= KeyEvent.KEYCODE_NUMPAD_0;
	public static final int NUM_1		= KeyEvent.KEYCODE_NUMPAD_1;
	public static final int NUM_2		= KeyEvent.KEYCODE_NUMPAD_2;
	public static final int NUM_3		= KeyEvent.KEYCODE_NUMPAD_3;
	public static final int NUM_4		= KeyEvent.KEYCODE_NUMPAD_4;
	public static final int NUM_5		= KeyEvent.KEYCODE_NUMPAD_5;
	public static final int NUM_6		= KeyEvent.KEYCODE_NUMPAD_6;
	public static final int NUM_7		= KeyEvent.KEYCODE_NUMPAD_7;
	public static final int NUM_8		= KeyEvent.KEYCODE_NUMPAD_8;
	public static final int NUM_9		= KeyEvent.KEYCODE_NUMPAD_9;

	public static final Signal<Key> event = new Signal<>(true);
	
	public static void processEvent(KeyEvent e) {
		if(e == null) { //shit happens
			return;
		}

		switch (e.getAction()) {
		case KeyEvent.ACTION_DOWN:
			event.dispatch(new Key(e.getKeyCode(), true));
			break;
		case KeyEvent.ACTION_UP:
			event.dispatch(new Key(e.getKeyCode(), false));
			break;
		}

	}
	
	public static String getKeyLabel(int keyCode) {
		// Map common keys to their display labels
		switch (keyCode) {
			case NUM_0: return "0";
			case NUM_1: return "1";
			case NUM_2: return "2";
			case NUM_3: return "3";
			case NUM_4: return "4";
			case NUM_5: return "5";
			case NUM_6: return "6";
			case NUM_7: return "7";
			case NUM_8: return "8";
			case NUM_9: return "9";
			case KeyEvent.KEYCODE_BACK: return "BK";
			case KeyEvent.KEYCODE_MENU: return "MN";
			case KeyEvent.KEYCODE_VOLUME_UP: return "V+";
			case KeyEvent.KEYCODE_VOLUME_DOWN: return "V-";
			default: return "?";  // Unknown key
		}
	}
	
	public static class Key {

		public final static int END_OF_FRAME = -1;
		public final static int BEGIN_OF_FRAME = -2;

		public int code;
		public boolean pressed;
		public long pressStartTime;

		public Key( int code, boolean pressed ) {
			this.code = code;
			this.pressed = pressed;
			this.pressStartTime = pressed ? System.currentTimeMillis() : 0;
		}
	}

}
