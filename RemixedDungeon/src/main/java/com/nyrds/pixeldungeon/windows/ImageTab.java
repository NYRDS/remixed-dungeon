package com.nyrds.pixeldungeon.windows;

import com.nyrds.platform.compatibility.RectF;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.windows.WndTabbed;
import com.watabou.pixeldungeon.windows.elements.Tab;

public class ImageTab extends Tab {

    protected Image icon;

    public ImageTab(WndTabbed parent, Image icon)  {
        super(parent);

        this.icon = icon;
        add(icon);
    }

    @Override
    public void select( boolean value ) {
        super.select( value );
        icon.am = selected ? 1.0f : 0.6f;
    }

    @Override
    protected void layout() {
        super.layout();

        //icon.copy( icon );
        icon.setX(x + (width - icon.width) / 2);
        icon.setY(y + (height - icon.height) / 2 - (selected ? 0 : 1));
        if (!selected && icon.getY() < y + CUT) {
            RectF frame = icon.frame();
            frame.top += (y + CUT - icon.getY()) / icon.texture.height;
            icon.frame( frame );
            icon.setY(y + CUT);
        }
    }
}