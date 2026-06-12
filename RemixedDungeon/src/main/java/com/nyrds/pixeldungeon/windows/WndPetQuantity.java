package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mechanics.PetInventoryManager;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.windows.IconTitle;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class WndPetQuantity extends Window {

    private static final int WIDTH = 120;
    private static final int BTN_HEIGHT = 18;
    private static final int[] QUANTITIES = {1, 5, 10, 50, 100, 500, 1000};

    private final VBox vbox = new VBox();
    private final Item item;
    private final Hero hero;
    private final Mob pet;
    private final boolean itemInPetInventory;

    public WndPetQuantity(@NotNull Item item, @NotNull Hero hero, @NotNull Mob pet) {
        super();

        this.item = item;
        this.hero = hero;
        this.pet = pet;
        this.itemInPetInventory = pet.getBelongings().backpack.contains(item) || pet.getBelongings().isEquipped(item);

        add(vbox);

        float pos = createDescription();

        vbox.clear();

        int maxQty = item.quantity();
        boolean hasMultipleOptions = false;

        for (int i = 0; i < QUANTITIES.length; ++i) {
            if (maxQty > QUANTITIES[i]) {
                final int qty = QUANTITIES[i];
                final int finalI = i;
                RedButton btn = new RedButton(getButtonText(qty, finalI)) {
                    @Override
                    protected void onClick() {
                        executeTransfer(qty);
                    }
                };
                btn.setSize(WIDTH, BTN_HEIGHT);
                vbox.add(btn);
                hasMultipleOptions = true;
            }
        }

        if (!hasMultipleOptions || maxQty > QUANTITIES[QUANTITIES.length - 1]) {
            RedButton btnAll = new RedButton(getButtonText(maxQty, -1)) {
                @Override
                protected void onClick() {
                    executeTransfer(maxQty);
                }
            };
            btnAll.setSize(WIDTH, BTN_HEIGHT);
            vbox.add(btnAll);
        }

        RedButton btnCancel = new RedButton(R.string.WndTradeItem_Cancel) {
            @Override
            protected void onClick() {
                hide();
            }
        };
        btnCancel.setSize(WIDTH, BTN_HEIGHT);
        vbox.add(btnCancel);

        vbox.setPos(0, pos + GAP);

        resize(WIDTH, (int) vbox.bottom());
    }

    private String getButtonText(int qty, int index) {
        if (itemInPetInventory) {
            // Taking from pet
            return Utils.format(R.string.PetInventory_TakeN, qty);
        } else {
            // Giving to pet
            return Utils.format(R.string.PetInventory_GiveN, qty);
        }
    }

    private void executeTransfer(int qty) {
        Item detached = item.detach(
            itemInPetInventory ? pet.getBelongings().backpack : hero.getBelongings().backpack,
            qty
        );

        if (detached == null) {
            hide();
            return;
        }

        boolean success;
        if (itemInPetInventory) {
            // Taking from pet to hero
            if (hero.getBelongings().isBackpackFull()) {
                GLog.w(StringsManager.getVar(R.string.PetInventory_HeroBackpackFull));
                detached.collect(pet.getBelongings().backpack);
                hide();
                return;
            }
            success = detached.collect(hero.getBelongings().backpack);
            if (success) {
                detached.setOwner(hero);
                GLog.i(StringsManager.getVar(R.string.PetInventory_TookItem), detached.name(), pet.getName());
            }
        } else {
            // Giving from hero to pet
            if (pet.getBelongings().isBackpackFull()) {
                GLog.w(StringsManager.getVar(R.string.PetInventory_PetBackpackFull));
                detached.collect(hero.getBelongings().backpack);
                hide();
                return;
            }
            if (detached instanceof EquipableItem && ((EquipableItem) detached).isEquipped(hero)) {
                GLog.w(StringsManager.getVar(R.string.PetInventory_CantGiveEquipped));
                detached.collect(hero.getBelongings().backpack);
                hide();
                return;
            }
            success = detached.collect(pet.getBelongings().backpack);
            if (success) {
                detached.setOwner(pet);
                GLog.i(StringsManager.getVar(R.string.PetInventory_GaveItem), detached.name(), pet.getName());
            }
        }

        if (success) {
            pet.updateSprite();
            hide();
            // Refresh parent bag window (current instance, usually pet's bag)
            if (WndBag.getInstance() != null) {
                WndBag.getInstance().updateItems();
            }
            // Also refresh hero's bag window if it exists (for give/take from hero's inventory)
            if (WndBag.getHeroBagInstance() != null) {
                WndBag.getHeroBagInstance().updateItems();
            }
        } else {
            // Restore on failure
            detached.collect(
                itemInPetInventory ? pet.getBelongings().backpack : hero.getBelongings().backpack
            );
        }
    }

    private float createDescription() {
        IconTitle titlebar = new IconTitle();
        titlebar.icon(new ItemSprite(item));
        titlebar.label(Utils.capitalize(item.toString()));
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

        return info.getY() + info.height();
    }
}