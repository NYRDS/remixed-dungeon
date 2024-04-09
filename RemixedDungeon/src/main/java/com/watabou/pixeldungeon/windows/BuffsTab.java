package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.windows.WndBuffInfo;
import com.nyrds.platform.input.Touchscreen;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.Util;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.CharModifier;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.ui.ImageButton;

import lombok.val;

class BuffsTab extends Group {

    private static final int GAP = 2;

    private float pos;

    public BuffsTab(final Char chr) {
        chr.forEachBuff(this::buffSlot);
    }

    private void buffSlot(CharModifier buff) {

        int index = buff.icon();

        if (index != BuffIndicator.NONE) {
            val icon = new ImageButton(new Image(TextureCache.get(buff.textureLarge()), 16, index)) {
                @Override
                protected void onClick() {
                    GameScene.show(new WndBuffInfo(buff));
                }
            };
            icon.setPos(GAP - 1, pos);

            Text txt = PixelScene.createText(buff.name(), GuiProperties.regularFontSize());
            txt.setX(icon.width() + (GAP * 2));
            txt.setY(pos + (int) (icon.height() - txt.baseLine()) / 2);
            val txtTouch = new TouchArea(txt) {
                @Override
                protected void onClick(Touchscreen.Touch touch) {
                    GameScene.show(new WndBuffInfo(buff));
                }
            };
            add(icon);
            add(txtTouch);
            add(txt);

            pos += GAP + icon.height();
        } else {
            if (Util.isDebug()) {
                Text txt = PixelScene.createText(buff.name(), GuiProperties.regularFontSize());
                txt.setX(GAP);
                txt.setY(pos + (int) (16 - txt.baseLine()) / 2);


                val txtTouch = new TouchArea(txt) {
                    @Override
                    protected void onClick(Touchscreen.Touch touch) {
                        GameScene.show(new WndBuffInfo(buff));
                    }
                };
                add(txtTouch);
                add(txt);

                pos += GAP + 16;
            }
        }
    }

    public float height() {
        return pos;
    }
}
