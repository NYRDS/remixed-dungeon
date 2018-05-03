package com.watabou.pixeldungeon.windows.elements;

import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Chrome;

public class Tool extends Button {

    private static final int BGCOLOR = 0x7B8073;

    static private final int SIZE = 16;
    protected Image     base;
    private   NinePatch bg;

    public Tool(String baseImageFile, int index, Chrome.Type chrome) {
        super();

        bg = Chrome.get(chrome);
        add(bg);

        base = new Image(baseImageFile,SIZE,index);
        bg.size(base.width + bg.marginHor(),base.height + bg.marginVer());
        width = height = SIZE;
        add(base);
    }

    @Override
    protected void layout() {
        super.layout();
            bg.x = x;
            bg.y = y;

            base.x = bg.x + bg.marginLeft();
            base.y = bg.y + bg.marginTop();
    }

    @Override
    public float width() {
        return bg.width();
    }

    @Override
    public float height() {
        return bg.height();
    }

    @Override
    protected void onTouchDown() {
        base.brightness(1.4f);
    }

    @Override
    protected void onTouchUp() {
        if (active) {
            base.resetColor();
        } else {
            base.tint(BGCOLOR, 0.7f);
        }
    }

    public void enable(boolean value) {
        if (value != active) {
            if (value) {
                base.resetColor();
            } else {
                base.tint(BGCOLOR, 0.7f);
            }
            active = value;
        }
    }
}