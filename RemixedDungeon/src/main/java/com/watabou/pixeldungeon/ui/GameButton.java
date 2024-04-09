package com.watabou.pixeldungeon.ui;

import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.utils.Utils;

public class GameButton extends RedButton {

    private Text secondary;

    public GameButton(String primary) {
        super(primary);

        secondary.text(Utils.EMPTY_STRING);
    }

    @Override
    protected void createChildren() {
        super.createChildren();

        secondary = PixelScene.createText(GuiProperties.smallFontSize());

        add(secondary);
    }

    @Override
    protected void layout() {
        super.layout();

        if (!secondary.text().isEmpty()) {
            text.setY(PixelScene.align(y
                    + (height - text.height() - secondary.height())
                    / 2));

            secondary.setX(PixelScene.align(x + (width - secondary.width()) / 2));
            secondary.setY(PixelScene.align(text.getY() + text.height()));
        } else {
            text.setY(PixelScene.align(y + (height - text.height()) / 2));
        }
    }

    public void secondary(String text) {
        secondary.text(text);
    }
}
