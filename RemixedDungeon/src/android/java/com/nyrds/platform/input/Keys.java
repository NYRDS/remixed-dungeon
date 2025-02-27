/*
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.nyrds.platform.input;

import android.view.KeyEvent;

import com.watabou.utils.Signal;

public class Keys {
	
	public static final int BACK		= KeyEvent.KEYCODE_BACK;
	public static final int MENU		= KeyEvent.KEYCODE_MENU;
	public static final int VOLUME_UP	= KeyEvent.KEYCODE_VOLUME_UP;
	public static final int VOLUME_DOWN	= KeyEvent.KEYCODE_VOLUME_DOWN;

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
