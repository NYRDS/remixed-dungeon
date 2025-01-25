
package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.mechanics.spells.SpellHelper;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.pixeldungeon.windows.VHBox;
import com.nyrds.pixeldungeon.windows.WndHeroSpells;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.elements.Tool;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class Toolbar extends Component {


    private final Tool btnWait;
    private final Tool btnSearch;
    private final Tool btnInfo;

    @Nullable
    private final Tool btnSpells;

    private final InventoryTool btnInventory;

    private Component toolbar = new Component();

    private final ArrayList<QuickslotTool> slots = new ArrayList<>();
    final private Hero hero;

    public Toolbar(@NotNull final Hero hero) {
        super();

        this.hero = hero;

        slots.add(new QuickslotTool(hero));

        btnWait = new Tool(7, Chrome.Type.ACTION_BUTTON) {
            @Override
            protected void onClick() {
                if (hero.isReady()) {
                    hero.rest(false);
                }
            }

            protected boolean onLongClick() {
                if (hero.isReady()) {
                    hero.rest(true);
                }
                return true;
            }
        };

        btnSearch = new Tool(8, Chrome.Type.ACTION_BUTTON) {
            @Override
            protected void onClick() {
                if (hero.isReady()) {
                    hero.search(true);
                }
            }
        };

        btnInfo = new Tool(9, Chrome.Type.ACTION_BUTTON) {
            @Override
            protected void onClick() {
                if (hero.isReady()) {
                    hero.selectCell(GameScene.informer);
                }
            }
        };

        btnSpells = new Tool(SpellHelper.iconIdByHero(hero), Chrome.Type.ACTION_BUTTON) {
            @Override
            protected void onClick() {
                if (hero.isReady()) {
                    GameScene.show(new WndHeroSpells(null));
                }
            }
        };

        btnInventory = new InventoryTool();
    }

    @Override
    protected void layout() {

        toolbar.removeAll();
        toolbar.destroy();

        int active_slots = GamePreferences.quickSlots();

        HBox slotBox = new HBox(width());
        slotBox.setAlign(HBox.Align.Center);

        if(active_slots<0) {
            int slotsCanFit = (int) (width()/slots.get(0).width());
            if(Math.abs(active_slots)!=slotsCanFit) {
                GamePreferences.quickSlots(-slotsCanFit);
                return;
            }
            active_slots = slotsCanFit;
        }

        for (int i = 0; i < active_slots; i++) {

            if(i>slots.size()-1) {
                slots.add(new QuickslotTool(hero));
            }

            slots.get(i).show(true);
            slotBox.add(slots.get(i));
        }

        if (slotBox.width() > width()) {
            GamePreferences.quickSlots(active_slots - 1);
            return;
        }


        VHBox actionBox = new VHBox(width());
        actionBox.add(btnWait);
        actionBox.add(btnSearch);
        actionBox.add(btnInfo);
        actionBox.setAlign(VBox.Align.Bottom);

        VHBox inventoryBox = new VHBox(width());
        if (hero.isSpellUser()) {
            inventoryBox.add(btnSpells);
        }
        inventoryBox.add(btnInventory);
        inventoryBox.setAlign(VBox.Align.Bottom);

        boolean handness = GamePreferences.handedness();

        actionBox.setAlign(handness ? HBox.Align.Right : HBox.Align.Left);
        inventoryBox.setAlign(handness ? HBox.Align.Right : HBox.Align.Left);

        if (slotBox.width() + actionBox.width() + inventoryBox.width() > width()) {
            actionBox.setMaxWidth(0);
            inventoryBox.setMaxWidth(0);
            actionBox.reset();
            inventoryBox.reset();
        }

        if (slotBox.width() + actionBox.width() + inventoryBox.width() > width()) {
            actionBox.setMaxWidth(width());
            inventoryBox.setMaxWidth(width());
            actionBox.reset();
            inventoryBox.reset();

            actionBox.wrapContent();
            inventoryBox.wrapContent();

            toolbar = new VBox();

            HBox buttonsBox = new HBox(width());
            buttonsBox.setAlign(HBox.Align.Width);
            buttonsBox.setAlign(VBox.Align.Bottom);

            if (handness) {
                buttonsBox.add(inventoryBox);
                buttonsBox.add(actionBox);
            } else {
                buttonsBox.add(actionBox);
                buttonsBox.add(inventoryBox);
            }

            toolbar.add(slotBox);
            toolbar.add(buttonsBox);

            ((VBox) toolbar).setAlign(VBox.Align.Bottom);
        } else {
            toolbar = new HBox(width());

            actionBox.wrapContent();
            inventoryBox.wrapContent();
            slotBox.wrapContent();

            if (handness) {
                toolbar.add(inventoryBox);
                toolbar.add(slotBox);
                toolbar.add(actionBox);
            } else {
                toolbar.add(actionBox);
                toolbar.add(slotBox);
                toolbar.add(inventoryBox);
            }

            ((HBox) toolbar).setAlign(HBox.Align.Width);
            ((HBox) toolbar).setAlign(VBox.Align.Bottom);
        }

        toolbar.setRect(x, camera.height - toolbar.height(), camera.width, toolbar.height());
        add(toolbar);
    }

    public void pickup(Item item) {
        btnInventory.pickUp(item);
    }

    @Override
    public float top() {
        return toolbar.top();
    }

}
