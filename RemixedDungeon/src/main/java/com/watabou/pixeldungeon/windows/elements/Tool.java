package com.watabou.pixeldungeon.windows.elements;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Chrome;

public class Tool extends Button {

    public enum Size{
      Std,Tiny
    }

    private static final int BGCOLOR = 0x7B8073;

    protected Image     base;
    private   NinePatch bg;

    public Tool(int index, Chrome.Type chrome) {
        this(Size.valueOf(GamePreferences.toolStyle()),index, chrome);
    }

    public Tool(Size size, int index, Chrome.Type chrome) {
        super();

        bg = Chrome.get(chrome);
        add(bg);

        String baseImageFile = Assets.UI_ICONS_12;
        int icon_size = 12;

        switch (size) {
            case Std:
                icon_size = 12;
                baseImageFile = Assets.UI_ICONS_12;
                break;
            case Tiny:
                icon_size = 6;
                baseImageFile = Assets.UI_ICONS_6;
                break;
        }

        if(index>=0) {
            base = new Image(baseImageFile, icon_size, index);
        } else {
            base = new ColorBlock(16,16, BGCOLOR);
        }
        width = base.width() + bg.marginHor();
        height = base.height() + bg.marginVer();

        bg.size(width,height);
        add(base);
    }

    @Override
    protected void layout() {
        super.layout();
            bg.setX(x);
            bg.setY(y);

            base.setX(bg.getX() + bg.marginLeft());
            base.setY(bg.getY() + bg.marginTop());
    }

    @Override
    protected void onTouchDown() {
        base.brightness(1.4f);
    }

    @Override
    protected void onTouchUp() {
        if (isActive()) {
            base.resetColor();
        } else {
            base.tint(BGCOLOR, 0.7f);
        }
    }

    public void enable(boolean value) {
        if (value != isActive()) {
            if (value) {
                base.resetColor();
            } else {
                base.tint(BGCOLOR, 0.7f);
            }
            setActive(value);
        }
    }
}