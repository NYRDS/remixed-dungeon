

package com.watabou.noosa;

import com.nyrds.platform.input.Touchscreen;
import com.nyrds.platform.input.Touchscreen.Touch;
import com.watabou.utils.Signal;

public class TouchArea extends Visual implements Signal.Listener<Touchscreen.Touch> {

    // Its target can be touch-area itself
    public final Visual target;

    private boolean catchTouch = true;

    protected Touchscreen.Touch touch = null;


    public TouchArea(Visual target) {
        super(0, 0, 0, 0);
        this.target = target;

        Touchscreen.event.add(this);
    }

    public TouchArea(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.target = this;

        setVisible(false);

        Touchscreen.event.add(this);
    }

    public void dontCatchTouch() {
        catchTouch = false;
    }

    @Override
    public void onSignal(Touch touch) {

        if (!isActive()) {
            return;
        }

        boolean hit = touch != null && target.overlapsScreenPoint((int) touch.start.x, (int) touch.start.y);

        if (hit) {
            if (catchTouch) {
                Touchscreen.event.cancel();
            }
            if (touch.down) {
                if (this.touch == null) {
                    this.touch = touch;
                }
                onTouchDown(touch);
            } else {
                onTouchUp(touch);
                if (this.touch == touch) {
                    this.touch = null;
                    onClick(touch);
                }
            }
        } else {
            if (touch == null && this.touch != null) {
                onDrag(this.touch);
            } else if (this.touch != null && !touch.down) {
                onTouchUp(touch);
                this.touch = null;
            }
        }
    }

    protected void onTouchDown(Touch touch) {
    }

    protected void onTouchUp(Touch touch) {
    }

    protected void onClick(Touch touch) {
    }

    protected void onDrag(Touch touch) {
    }

    public void reset() {
        touch = null;
    }

    @Override
    public void destroy() {
        Touchscreen.event.remove(this);
        super.destroy();
    }
}
