package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.mechanics.PetInventoryManager;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.items.EquipableItem;
import com.nyrds.pixeldungeon.items.Item;
import com.nyrds.pixeldungeon.actors.hero.Hero;
import com.nyrds.pixeldungeon.actors.mobs.Mob;
import com.nyrds.pixeldungeon.actors.hero.Belongings;
import com.nyrds.pixeldungeon.scenes.GameScene;
import com.nyrds.pixeldungeon.ml.actions.UseItem;
import com.nyrds.pixeldungeon.ml.actions.CharAction;
import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VHBox;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.Util;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class WndPetItem extends Window {

    private static final float BUTTON_WIDTH = 36;
    private VHBox actions;

    private final Hero hero;
    private final Mob pet;
    private final Item item;
    private final boolean itemInPetInventory;

    public WndPetItem(@NotNull Hero hero, @NotNull Mob pet, @NotNull Item item) {

        super();

        int WIDTH = stdWidth();

        this.hero = hero;
        this.pet = pet;
        this.item = item;
        this.itemInPetInventory = pet.getBelongings().backpack.contains(item) || pet.getBelongings().isEquipped(item);

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
        if (Util.isDebug()) {
            if (item.cooldown() < Util.BIG_FLOAT / 2) {
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

        // Add pet-specific actions
        addPetActions();

        add(actions);
        actions.setPos(titlebar.left(), y);

        resize(WIDTH, (int) (actions.bottom() + GAP));
    }

    private void addPetActions() {
        if (itemInPetInventory) {
            // Item is in pet's inventory - can take, equip, or unequip
            if (pet.getBelongings().isEquipped(item)) {
                // Item is equipped on pet - can unequip
                addActionButton(CommonActions.AC_UNEQUIP_FROM_PET, StringsManager.maybeId(CommonActions.AC_UNEQUIP_FROM_PET));
            } else {
                // Item is in pet's backpack - can take or equip
                addActionButton(CommonActions.AC_TAKE_FROM_PET, StringsManager.maybeId(CommonActions.AC_TAKE_FROM_PET));
                
                if (item instanceof EquipableItem) {
                    EquipableItem equipable = (EquipableItem) item;
                    Belongings.Slot slot = equipable.slot(pet.getBelongings());
                    if (slot != Belongings.Slot.NONE && !pet.getBelongings().slotBlocked(slot)) {
                        addActionButton(CommonActions.AC_EQUIP_ON_PET, StringsManager.maybeId(CommonActions.AC_EQUIP_ON_PET));
                    }
                }
            }
        } else {
            // Item is in hero's inventory - can give to pet
            if (!hero.getBelongings().isEquipped(item)) {
                addActionButton(CommonActions.AC_GIVE_TO_PET, StringsManager.maybeId(CommonActions.AC_GIVE_TO_PET));
            }
        }
    }

    private void addActionButton(final String actionId, String displayName) {
        RedButton btn = new RedButton(displayName) {
            @Override
            protected void onClick() {
                executeAction(actionId);
                hide();
            }
        };
        btn.setSize(Math.max(BUTTON_WIDTH, btn.reqWidth()), BUTTON_HEIGHT);
        actions.add(btn);
    }

    private void executeAction(String action) {
        switch (action) {
            case CommonActions.AC_GIVE_TO_PET:
                PetInventoryManager.giveItemToPet(hero, pet, item);
                break;
            case CommonActions.AC_TAKE_FROM_PET:
                PetInventoryManager.takeItemFromPet(hero, pet, item);
                break;
            case CommonActions.AC_EQUIP_ON_PET:
                if (item instanceof EquipableItem) {
                    EquipableItem equipable = (EquipableItem) item;
                    Belongings.Slot slot = equipable.slot(pet.getBelongings());
                    PetInventoryManager.equipItemOnPet(pet, equipable, slot);
                }
                break;
            case CommonActions.AC_UNEQUIP_FROM_PET:
                if (item instanceof EquipableItem) {
                    PetInventoryManager.unequipItemFromPet(pet, (EquipableItem) item);
                }
                break;
        }
    }
}