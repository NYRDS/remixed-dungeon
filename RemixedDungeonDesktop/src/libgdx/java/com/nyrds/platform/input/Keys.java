package com.nyrds.platform.input;

import android.view.KeyEvent;

import com.watabou.utils.Signal;

public class Keys {
	
	public static final int BACK		= KeyEvent.KEYCODE_BACK;
	public static final int MENU		= KeyEvent.KEYCODE_MENU;
	public static final int VOLUME_UP	= KeyEvent.KEYCODE_VOLUME_UP;
	public static final int VOLUME_DOWN	= KeyEvent.KEYCODE_VOLUME_DOWN;

	public static Signal<Key> event = new Signal<>(true);
	
	public static void processEvent(KeyEvent e) {
		switch (e.getAction()) {
		case KeyEvent.ACTION_DOWN:
			event.dispatch(new Key(e.getKeyCode(), true));
			break;
		case KeyEvent.ACTION_UP:
			event.dispatch(new Key(e.getKeyCode(), false));
			break;
		}
	}
	
	public static class Key {

		public final static int END_OF_FRAME = -1;
		public final static int BEGIN_OF_FRAME = -2;

		public int code;
		public boolean pressed;
		
		public Key( int code, boolean pressed ) {
			this.code = code;
			this.pressed = pressed;
		}
	}
}
