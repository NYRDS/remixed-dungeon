package com.watabou.pixeldungeon.ui;

import com.nyrds.platform.audio.Sample;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.scenes.PixelScene;

public class DashboardItem extends Button {

    public static final float SIZE = 48;

    public static final int IMAGE_SIZE = 32;

    private Image image;
    private Text label;

    public DashboardItem(String text, int index) {
        super();

        image.frame(image.texture.uvRect(index * IMAGE_SIZE, 0, (index + 1)
                * IMAGE_SIZE, IMAGE_SIZE));
        this.label.text(text);

        setSize(SIZE, SIZE);
    }

    @Override
    protected void createChildren() {
        super.createChildren();

        image = new Image(Assets.DASHBOARD);
        add(image);

        label = PixelScene.createText(GuiProperties.titleFontSize());

        add(label);
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
