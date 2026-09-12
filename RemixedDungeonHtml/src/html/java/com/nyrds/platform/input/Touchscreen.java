

package com.nyrds.platform.input;

import com.watabou.utils.PointF;
import com.watabou.utils.Signal;
import java.util.HashMap;

public class Touchscreen {

    public static Signal<Touch> event = new Signal<>(true);

    public static HashMap<Integer, Touch> pointers = new HashMap<>();

    public static void processEvent(PointerEvent e) {

        Touch touch;

        switch (e.type) {

            case TOUCH_DOWN:
                touch = new Touch(e);
                pointers.put(e.ptr, touch);
                event.dispatch(touch);
                break;

            case TOUCH_DRAGGED:
                touch = pointers.get(e.ptr);
                if (touch != null) {
                    touch.update(e);
                    event.dispatch(null);
                }
                break;

            case TOUCH_UP:
                Touch t = pointers.remove(e.ptr);
                if (t != null) {
                    event.dispatch(t.up());
                }
                break;

        }
    }

    public static class Touch {

        public PointF start;
        public PointF current;
        public boolean down;

        public Touch(PointerEvent e) {
            start = new PointF(e.x, e.y);
            current = new PointF(e.x, e.y);
            down = true;
        }

        public void update(PointerEvent e) {
            current.set(e.x, e.y);
        }

        public Touch up() {
            down = false;
            return this;
        }
    }
}
