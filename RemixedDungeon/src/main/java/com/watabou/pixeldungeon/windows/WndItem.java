
package com.watabou.pixeldungeon.windows;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.actions.CharAction;
import com.nyrds.pixeldungeon.ml.actions.UseItem;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VHBox;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.Util;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

public class WndItem extends Window {

    private static final float BUTTON_WIDTH = 36;
    private VHBox actions;

    public WndItem(final WndBag bag, final Item item) {

        super();

        int WIDTH = stdWidth();

        IconTitle titlebar = new IconTitle(new ItemSprite(item), Utils.capitalize(item.toString()));
        titlebar.setRect(0, 0, WIDTH, 0);
        add(titlebar);

        if (item.isLevelKnown()) {
            int level = item.level();
            if (level > 0) {
                titlebar.color(ItemSlot.UPGRADED);
            } else if (level < 0) {
                titlebar.color(ItemSlot.DEGRADED);
            }
        }

        Text info = PixelScene.createMultiline(item.info(), GuiProperties.regularFontSize());
        if(Util.isDebug()) {
            if(item.cooldown()<Util.BIG_FLOAT/2) {
                info.text(info.text() + Utils.format("\ncooldown %.1f\nowner %s", item.cooldown(), item.getOwner().getEntityKind()));
            }
        }
        info.maxWidth(WIDTH);
        info.setX(titlebar.left());
        info.setY(titlebar.bottom() + GAP);
        add(info);

        float y = info.getY() + info.height() + GAP;

        actions = new VHBox(WIDTH);
        actions.setAlign(HBox.Align.Width);
        actions.setGap(GAP);

        Char owner = item.getOwner();

        if (bag != null && owner.isAlive()) {
            for (final String action : item.actions(owner)) {

                if (owner.getHeroClass().forbidden(action)) {
                    continue;
                }

                RedButton btn = new RedButton(StringsManager.maybeId(action)) {
                    @Override
                    protected void onClick() {
                        CharAction acton = new UseItem(item, action);
                        acton.act(owner);

                        hide();

                        if (!CommonActions.hideBagOnAction(action)) {
                            if (bag.getActiveDialog() == null) {
                                bag.updateItems();
                            }
                        } else {
                            bag.hide();
                        }
                    }
                };
                btn.setSize(Math.max(BUTTON_WIDTH, btn.reqWidth()), BUTTON_HEIGHT);

                actions.add(btn);
            }
        }

        add(actions);
        actions.setPos(titlebar.left(), y);

        resize(WIDTH, (int) (actions.bottom() + GAP));
    }

    public WndItem(final Item item, Char actor) {

        super();

        int WIDTH = stdWidth();

        IconTitle titlebar = new IconTitle(new ItemSprite(item), Utils.capitalize(item.toString()));
        titlebar.setRect(0, 0, WIDTH, 0);
        add(titlebar);

        if (item.isLevelKnown()) {
            int level = item.level();
            if (level > 0) {
                titlebar.color(ItemSlot.UPGRADED);
            } else if (level < 0) {
                titlebar.color(ItemSlot.DEGRADED);
            }
        }

        Text info = PixelScene.createMultiline(item.info(), GuiProperties.regularFontSize());
        info.maxWidth(WIDTH);
        info.setX(titlebar.left());
        info.setY(titlebar.bottom() + GAP);
        add(info);

        float y = info.getY() + info.height() + GAP;

        actions = new VHBox(WIDTH);
        actions.setAlign(HBox.Align.Width);
        actions.setGap(GAP);


        for (final String action : item.actions(actor)) {

            if (actor.getHeroClass().forbidden(action)) {
                continue;
            }

            RedButton btn = new RedButton(StringsManager.maybeId(action)) {
                @Override
                protected void onClick() {
                    CharAction acton = new UseItem(item, action);
                    acton.act(actor);

                    hide();
                }
            };
            btn.setSize(Math.max(BUTTON_WIDTH, btn.reqWidth()), BUTTON_HEIGHT);

            actions.add(btn);
        }


        add(actions);
        actions.setPos(titlebar.left(), y);

        resize(WIDTH, (int) (actions.bottom() + GAP));
    }

   @LuaInterface
   public  void onSelect(int idx) {
        ((RedButton)actions.getByIndex(idx)).simulateClick();
    }

}
