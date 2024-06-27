package com.watabou.pixeldungeon.ui;

import com.nyrds.platform.audio.Sample;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.scenes.PixelScene;

public class ImageTextButton extends Button {

    public static final float SIZE = 48;

    private final Image image;
    private final Text label;

    public ImageTextButton(String text, Image image) {
        super();

        this.image = image;

        add(this.image);

        label = PixelScene.createText(GuiProperties.titleFontSize());
        label.text(text);
        add(label);

        setSize(SIZE, SIZE);
    }

    @Override
    protected void layout() {
        super.layout();

        image.setX(PixelScene.align(x + (width - image.width()) / 2));
        image.setY(PixelScene.align(y));

        label.setX(PixelScene.align(x + (width - label.width()) / 2));
        label.setY(PixelScene.align(image.getY() + image.height() + 2));
    }

    @Override
    protected void onTouchDown() {
        image.brightness(1.5f);
        Sample.INSTANCE.play(Assets.SND_CLICK, 1, 1, 0.8f);
    }

    @Override
    protected void onTouchUp() {
        image.resetColor();
    }
}
