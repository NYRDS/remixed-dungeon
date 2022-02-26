package com.nyrds.pixeldungeon.windows;

import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.utils.Utils;

/**
 * Created by mike on 15.05.2018.
 * This file is part of Remixed Pixel Dungeon.
 */
public class RedImageButton extends RedButton {

    public RedImageButton(Image image) {
        super(Utils.EMPTY_STRING);
        icon(image);
    }

    @Override
    protected void layout() {
        super.layout();

        icon.setX(x + (width - icon.width()) / 2);
        icon.setY(y + (height - icon.height()) / 2);
    }
}
