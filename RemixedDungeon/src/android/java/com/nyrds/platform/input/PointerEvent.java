package com.nyrds.platform.input;

import android.view.MotionEvent;

public class PointerEvent {

    public MotionEvent event;

    public PointerEvent(MotionEvent e) {
        event = e;
    }
}
