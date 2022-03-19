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

import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.PointF;
import com.watabou.utils.Signal;

import java.util.HashMap;

public class Touchscreen {
	
	public static Signal<Touch> event = new Signal<>(true);

	public static HashMap<Integer,Touch> pointers = new HashMap<>();

	public static float x;
	public static float y;
	public static boolean touched;

	public static void processEvent(PointerEvent e) {

		GLog.debug("pe: %d %d %d %s", e.x, e.y, e.ptr, e.type.name());

		Touch touch;

		switch (e.type) {

		case TOUCH_DOWN:
			touched = true;
			touch = new Touch(e);
			pointers.put( e.ptr, touch );
			event.dispatch( touch );
			break;

		case TOUCH_UP:
			touched = false;
			event.dispatch(pointers.remove(e.ptr).up());
			break;

		}
	}
	
	public static class Touch {
		
		public PointF start;
		public PointF current;
		public boolean down;
		
		public Touch( PointerEvent e) {
			
			float x = e.x;
			float y = e.y;
			
			start = new PointF( x, y );
			current = new PointF( x, y );
			
			down = true;
		}
		
		public void update( PointerEvent e) {
			current.set( e.x, e.y );
		}
		
		public Touch up() {
			down = false;
			return this;
		}
	}

}
