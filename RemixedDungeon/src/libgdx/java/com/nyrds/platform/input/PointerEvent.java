package com.nyrds.platform.input;

public class PointerEvent {

    public int x;
    public int y;
    public int ptr;
    public Type type;

    public PointerEvent(int screenX, int screenY, int pointer, int button, Type type) {
        x = screenX;
        y = screenY;
        ptr = pointer;
        this.type = type;
    }

    public enum Type {
        TOUCH_DOWN,
        TOUCH_UP
    }
}
