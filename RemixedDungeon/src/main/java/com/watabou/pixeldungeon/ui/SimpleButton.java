
package com.watabou.pixeldungeon.ui;

import com.nyrds.platform.input.Touchscreen.Touch;
import com.watabou.noosa.Image;
import com.watabou.noosa.TouchArea;
import com.watabou.noosa.ui.Component;

public class SimpleButton extends Component {

    private Image image;

    public SimpleButton(Image image) {
        super();

        this.image.copy(image);
        width = image.width;
        height = image.height;
    }

    @Override
    protected void createChildren() {
        image = new Image();
        add(image);

        add(new TouchArea(image) {
            @Override
            protected void onTouchDown(Touch touch) {
                image.brightness(1.2f);
            }

            @Override
            protected void onTouchUp(Touch touch) {
                image.brightness(1.0f);
            }

            @Override
            protected void onClick(Touch touch) {
                SimpleButton.this.onClick();
            }
        });
    }

    @Override
    protected void layout() {
        image.setX(x + (width - image.width()) / 2);
        image.setY(y + (height - image.height()) / 2);
    }

    protected void onClick() {
    }

    public void enable(boolean value) {
        setActive(value);
        image.alpha(value ? 1.0f : 0.3f);
    }


}
