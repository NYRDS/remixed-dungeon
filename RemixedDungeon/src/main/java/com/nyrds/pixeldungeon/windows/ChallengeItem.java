package com.nyrds.pixeldungeon.windows;

import static com.watabou.pixeldungeon.ui.Window.GAP;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.Util;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.CompositeTextureImage;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.GameLog;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.ImageButton;
import com.watabou.pixeldungeon.ui.ListItem;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndStory;
import com.watabou.utils.Callback;

public class ChallengeItem extends Component {

    protected ColorBlock bg = new ColorBlock(width, height, 0xFF4A4D44);
    protected ImageButton descIcon;
    protected ImageButton itemIcon;

    private boolean state = true;

    protected Text label     = PixelScene.createText(GuiProperties.regularFontSize());

    HBox box;
    Callback onClickCallback;

    ChallengeItem(Image icon, String title, String desc, Image _descIcon, float maxWidth, Callback onClick) {

        onClickCallback = onClick;

        itemIcon = new ImageButton(icon) {
            @Override
            protected void onClick() {
                state = ! state;
                if (state) {
                    icon.brightness(1.0f);
                } else {
                    icon.brightness(0.5f);
                }
            }
        };

        descIcon = new ImageButton(_descIcon) {
            @Override
            protected void onClick() {
                GameLoop.addToScene(new WndStory(desc));
            }
        };

        box = new HBox(maxWidth - 2 * GAP);
        box.setAlign(HBox.Align.Width);
        box.setAlign(VBox.Align.Center);

        box.add(itemIcon);
        box.add(label);
        box.add(descIcon);

        label.text(title);

        add(bg);
        add(box);
    }

    @Override
    protected void layout() {
        box.setPos(x + GAP,y + GAP);
        bg.setX(x);
        bg.setY(y);
        bg.size(box.getMaxWidth() + 2 * GAP, box.height() + 2 * GAP);
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void measure() {
        box.measure();
    }


    @Override
    public float width() {
        return box.width();
    }

    @Override
    public float height() {
        return box.height() + 4*GAP;
    }
}
