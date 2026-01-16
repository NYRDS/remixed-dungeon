

package com.nyrds.platform.input;

import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.PointF;
import com.watabou.utils.Signal;

import java.util.HashMap;

public class Touchscreen {

    public static Signal<Touch> event = new Signal<>(true);

    public static HashMap<Integer, Touch> pointers = new HashMap<>();

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
                touched = false;
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

            float x = e.x;
            float y = e.y;

            start = new PointF(x, y);
            current = new PointF(x, y);

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
