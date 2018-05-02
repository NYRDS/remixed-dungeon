package com.watabou.pixeldungeon.windows.elements;

import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Chrome;

public class Tool extends Button {

    private static final int BGCOLOR = 0x7B8073;

    static private final int SIZE = 16;
    private Image     base;
    private NinePatch bg;

    public Tool(String baseImageFile, int x, int y, int width, int height) {
        super();
        base = new Image(baseImageFile);
        base.frame(x, y, width, height);



        this.width = width;
        this.height = height;
        add(base);
    }

    public Tool(String baseImageFile, int index) {
        super();

        bg = Chrome.get(Chrome.Type.BUTTON);
        add(bg);

        base = new Image(baseImageFile,SIZE,index);
        width = height = SIZE;
        add(base);
        layout();
    }

    public Tool(int x, int y, int width, int height) {
        this(Assets.getToolbar(), x, y, width, height);
    }

    @Override
    protected void layout() {
        super.layout();

        if(bg!=null) {
            bg.x = x;
            bg.y = y;
            bg.size(base.width + bg.marginHor(),base.height + bg.marginVer());

            base.x = bg.x + bg.marginLeft();
            base.y = bg.y + bg.marginTop();
        } else {
            base.x = x;
            base.y = y;
        }
    }

    @Override
    public float width() {
        if(bg!=null) {
            return bg.width();
        }
        return super.width();
    }

    @Override
    public float height() {
        if(bg!=null) {
            return bg.height();
        }
        return super.height();
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