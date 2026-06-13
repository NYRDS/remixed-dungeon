package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mechanics.PetInventoryManager;
import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.windows.ItemPlaceholder;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.elements.Tab;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WndPetBag extends WndBag {

    private final Hero hero;
    private final Mob pet;
    
    // Track current instance for refresh after item operations
    private static WndPetBag currentInstance;

    public WndPetBag(@NotNull Hero hero, @NotNull Mob pet) {
        // Use a temporary listener for super() call, will replace after
        super(pet.getBelongings(), pet.getBelongings().backpack, new PetBagListener(hero, pet, null), Mode.ALL,
              Utils.capitalize(pet.getName()) + " " + Utils.format(R.string.WndPetBag_HP, pet.hp(), pet.ht()));

        this.hero = hero;
        this.pet = pet;
        currentInstance = this;

        // Now create and set the real listener with bag reference
        PetBagListener listener = new PetBagListener(hero, pet, this);
        setListener(listener);

        // Parent's placeItems() placed backpack items but NOT equipped items for pets
        // We need to clear and re-place everything in correct order: equipped first, then backpack
        clearAndReplaceItems();

        // Add equippability indicators to existing item slots
        addEquippabilityIndicators();
    }
    
    public static void refreshCurrent() {
        if (currentInstance != null) {
            currentInstance.clearAndReplaceItems();
            currentInstance.addEquippabilityIndicators();
        }
    }
    
    @Override
    public void hide() {
        if (currentInstance == this) {
            currentInstance = null;
        }
        super.hide();
    }

    private void clearAndReplaceItems() {
        // Use protected clearItems() from parent
        clearItems();

        // Place equipped items for pet (weapon, armor, left hand, artifact, left artifact)
        Belongings belongings = pet.getBelongings();
        placeEquippedItem(belongings.getItemFromSlot(Belongings.Slot.WEAPON), Belongings.Slot.WEAPON, ItemPlaceholder.RIGHT_HAND);
        placeEquippedItem(belongings.getItemFromSlot(Belongings.Slot.ARMOR), Belongings.Slot.ARMOR, ItemPlaceholder.BODY);
        placeEquippedItem(belongings.getItemFromSlot(Belongings.Slot.LEFT_HAND), Belongings.Slot.LEFT_HAND, ItemPlaceholder.LEFT_HAND);
        placeEquippedItem(belongings.getItemFromSlot(Belongings.Slot.ARTIFACT), Belongings.Slot.ARTIFACT, ItemPlaceholder.ARTIFACT);
        placeEquippedItem(belongings.getItemFromSlot(Belongings.Slot.LEFT_ARTIFACT), Belongings.Slot.LEFT_ARTIFACT, ItemPlaceholder.ARTIFACT);

        // Place backpack items
        for (Item item : belongings.backpack.items) {
            if (!(item instanceof Gold)) {
                placeBackpackItem(item);
            }
        }

        // Place empty slots
        int margin = 5;
        while (count - margin < belongings.backpack.getSize()) {
            placeBackpackItem(ItemsList.DUMMY);
        }

        // Place gold
        Gold gold = belongings.getItem(Gold.class);
        if (gold != null) {
            row = nRows - 1;
            col = nCols - 1;
            placeBackpackItem(gold);
        }
    }

    private void placeEquippedItem(@Nullable Item item, Belongings.Slot slot, int placeholderImage) {
        if (item == null || item == com.nyrds.pixeldungeon.utils.ItemsList.DUMMY) {
            // Empty slot - show placeholder (locked or empty)
            Belongings belongings = pet.getBelongings();
            if (belongings.slotBlocked(slot)) {
                placeItem(new ItemPlaceholder(ItemPlaceholder.LOCKED));
            } else {
                placeItem(new ItemPlaceholder(placeholderImage));
            }
            return;
        }
        // Use protected placeEquipped with named constants from ItemPlaceholder
        placeEquipped(item, slot, placeholderImage);
    }

    private void placeBackpackItem(@NotNull Item item) {
        if (row >= nRows) return;
        placeItem(item);
    }

    private void addEquippabilityIndicators() {
        // Iterate through all children to find ItemSlot instances
        for (com.watabou.noosa.Gizmo child : members) {
            if (child instanceof ItemSlot) {
                ItemSlot slot = (ItemSlot) child;
                // We can't easily get the item from ItemSlot (no getter)
                // Instead, check pet's backpack items and match by position
                // For now, we'll use reflection to access the private item field in ItemButton
                addEquippabilityIndicatorToSlot(slot);
            }
        }
    }

    private void addEquippabilityIndicatorToSlot(@NotNull ItemSlot slot) {
        // Try to get the item via reflection from ItemButton (which extends ItemSlot)
        try {
            // ItemButton has a private 'item' field
            java.lang.reflect.Field itemField = slot.getClass().getDeclaredField("item");
            itemField.setAccessible(true);
            Item item = (Item) itemField.get(slot);

            if (item != null && item.valid() && item instanceof EquipableItem) {
                addEquippabilityIndicator(slot, (EquipableItem) item);
            }
        } catch (Exception e) {
            // Not an ItemButton or reflection failed, silently continue
        }
    }

    private void addEquippabilityIndicator(@NotNull ItemSlot slot, @NotNull EquipableItem item) {
        Belongings.Slot equipSlot = item.slot(pet.getBelongings());
        if (equipSlot == Belongings.Slot.NONE) {
            return; // Not equippable by this pet
        }

        boolean canEquip = item.statsRequirementsSatisfied() && !pet.getBelongings().slotBlocked(equipSlot);

        // Use the existing topRight BitmapText to show equippability indicator
        // We need to access it via reflection since it's private
        try {
            java.lang.reflect.Field topRightField = ItemSlot.class.getDeclaredField("topRight");
            topRightField.setAccessible(true);
            com.watabou.noosa.BitmapText topRight = (com.watabou.noosa.BitmapText) topRightField.get(slot);

            String indicator = getSlotIcon(equipSlot);
            if (indicator != null) {
                topRight.text(canEquip ? indicator : "\u26A0"); // Warning sign if can't equip
                topRight.setScaleXY(0.8f, 0.8f);
                topRight.hardlight(canEquip ? 0x44FF44 : 0xFF4444); // Green if can equip, red if can't
            }
        } catch (Exception e) {
            // Reflection failed, silently continue
        }
    }

    private String getSlotIcon(@NotNull Belongings.Slot slot) {
        switch (slot) {
            case WEAPON:
                return "\u2694"; // ⚔️
            case ARMOR:
                return "\uD83D\uDCA1"; // 🛡️
            case LEFT_HAND:
            case ARTIFACT:
            case LEFT_ARTIFACT:
                return "\uD83D\uDC8D"; // 💍
            default:
                return null;
        }
    }

    @Override
    public void onClick(Tab tab) {
        super.onClick(tab);
        // Update title with current HP when switching tabs
        updatePetTitle();
        // Re-add indicators after tab switch (items are recreated)
        addEquippabilityIndicators();
    }

    private void updatePetTitle() {
        // Access private txtTitle field via reflection
        try {
            java.lang.reflect.Field txtTitleField = WndBag.class.getDeclaredField("txtTitle");
            txtTitleField.setAccessible(true);
            com.watabou.noosa.Text txtTitle = (com.watabou.noosa.Text) txtTitleField.get(this);
            if (txtTitle != null) {
                txtTitle.text(Utils.capitalize(pet.getName()) + " " + Utils.format(R.string.WndPetBag_HP, pet.hp(), pet.ht()));
                // Re-center the title
                java.lang.reflect.Field panelWidthField = WndBag.class.getDeclaredField("panelWidth");
                panelWidthField.setAccessible(true);
                int panelWidth = (int) panelWidthField.get(this);
                txtTitle.setX(com.watabou.pixeldungeon.scenes.PixelScene.align((panelWidth - txtTitle.width()) / 2));
            }
        } catch (Exception e) {
            // Reflection failed, silently continue
        }
    }

    @Override
    public void onSignal(com.nyrds.platform.input.Keys.Key key) {
        if (key.pressed) {
            switch (key.code) {
                case android.view.KeyEvent.KEYCODE_I:
                case android.view.KeyEvent.KEYCODE_BACK:
                    hide();
                    break;
            }
        }
        super.onSignal(key);
    }

    @Override
    public void onBackPressed() {
        if (getListener() != null) {
            getListener().onSelect(null, hero);
        }
        super.onBackPressed();
    }

    private static class PetBagListener implements WndBag.Listener {
        private final Hero hero;
        private final Mob pet;
        private WndBag bag;

        public PetBagListener(Hero hero, Mob pet, WndBag bag) {
            this.hero = hero;
            this.pet = pet;
            this.bag = bag;
        }

        public void setBag(WndBag bag) {
            this.bag = bag;
        }

        @Override
        public void onSelect(Item item, Char selector) {
            if (item != null && item.valid()) {
                // Show quantity selector for stackable items
                if (item.quantity() > 1) {
                    GameScene.show(new WndPetQuantity(item, hero, pet));
                } else {
                    // Show WndItem with pet-specific actions (pass bag reference for updateItems)
                    GameScene.show(new WndPetItem(bag, hero, pet, item));
                }
            }
        }
    }
}