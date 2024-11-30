package com.nyrds.platform.input;

import android.view.MotionEvent;

import com.watabou.utils.PointF;
import com.watabou.utils.Signal;

import java.util.HashMap;

public class Touchscreen {

	public static Signal<Touch> event = new Signal<>(true);

	public static HashMap<Integer, Touch> pointers = new HashMap<>();

	public static float x;
	public static float y;
	public static boolean touched;

	public static void processEvent(PointerEvent ev) {

		Touch touch;

		MotionEvent e = ev.event;

		switch (e.getAction() & MotionEvent.ACTION_MASK) {

			case MotionEvent.ACTION_DOWN:
				touched = true;
				touch = new Touch(e, 0);
				pointers.put(e.getPointerId(0), touch);
				event.dispatch(touch);
				break;

			case MotionEvent.ACTION_POINTER_DOWN:
				int index = e.getActionIndex();
				touch = new Touch( e, index );
				pointers.put( e.getPointerId( index ), touch );
				event.dispatch( touch );
				break;

			case MotionEvent.ACTION_MOVE:
				int count = e.getPointerCount();
				for (int j=0; j < count; j++) {
					pointers.get( e.getPointerId( j ) ).update( e, j );
				}
				event.dispatch( null );
				break;

			case MotionEvent.ACTION_POINTER_UP:
				event.dispatch( pointers.remove( e.getPointerId( e.getActionIndex() ) ).up() );
				break;

			case MotionEvent.ACTION_UP:
				touched = false;
				event.dispatch( pointers.remove( e.getPointerId( 0 ) ).up() );
				break;

		}

		e.recycle();
	}

	public static class Touch {

		public PointF start;
		public PointF current;
		public boolean down;

		public Touch( MotionEvent e, int index ) {

			float x = e.getX( index );
			float y = e.getY( index );

			start = new PointF( x, y );
			current = new PointF( x, y );

			down = true;
		}

		public void update( MotionEvent e, int index ) {
			current.set( e.getX( index ), e.getY( index ) );
		}

		public Touch up() {
			down = false;
			return this;
		}
	}

}
