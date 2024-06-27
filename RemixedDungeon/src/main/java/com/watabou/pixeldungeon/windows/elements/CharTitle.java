package com.watabou.pixeldungeon.windows.elements;

import com.nyrds.util.GuiProperties;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class CharTitle extends Component {

    private static final int COLOR_BG = 0xFFCC0000;
    private static final int COLOR_LVL = 0xFF00EE00;

    private static final int BAR_HEIGHT = 2;

    private final CharSprite image;
    private final Text name;
    private final ColorBlock hpBg;
    private final ColorBlock hpLvl;
    private final BuffIndicator buffs;

    private final float hp;

    public CharTitle(@NotNull Char mob) {

        hp = (float) mob.hp() / mob.ht();

        name = PixelScene.createText(Utils.capitalize(mob.getName()), GuiProperties.titleFontSize());
        name.hardlight(Window.TITLE_COLOR);
        add(name);

        image = mob.newSprite();
        add(image);

        hpBg = new ColorBlock(1, 1, COLOR_BG);
        add(hpBg);

        hpLvl = new ColorBlock(1, 1, COLOR_LVL);
        add(hpLvl);

        buffs = new BuffIndicator(mob);
        add(buffs);
    }

    @Override
    protected void layout() {

        image.setX(0 - image.visualOffsetX());
        image.setY(Math.max(0, name.height() + Window.GAP + BAR_HEIGHT - image.visualHeight()) - image.visualOffsetY());

        float x = image.visualWidth() + Window.GAP;
        name.setX(x);
        name.setY(image.visualHeight() - BAR_HEIGHT - Window.GAP - name.baseLine());

        float w = width - image.width() - image.x - Window.GAP;

        hpBg.size(w, BAR_HEIGHT);
        hpLvl.size(w * hp, BAR_HEIGHT);

        hpBg.setX(x);
        hpLvl.setX(x);
        float y = image.visualHeight() - BAR_HEIGHT;
        hpLvl.setY(y);
        hpBg.setY(y);

        buffs.setPos(
                name.getX() + name.width() + Window.GAP,
                name.getY() + name.baseLine() - BuffIndicator.ICON_SIZE);

        height = hpBg.getY() + hpBg.height();
    }
}
