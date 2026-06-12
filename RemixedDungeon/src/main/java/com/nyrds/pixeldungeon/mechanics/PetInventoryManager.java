package com.nyrds.pixeldungeon.mechanics;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.nyrds.pixeldungeon.windows.WndPetBag;
import com.nyrds.pixeldungeon.windows.WndPetSelect;
import com.watabou.pixeldungeon.windows.WndBag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PetInventoryManager {

    private PetInventoryManager() {
        // Utility class
    }

    /**
     * Checks if the hero can interact with the pet's inventory.
     * Requires: pet is alive, pet is owned by hero, pet is adjacent or same cell.
     */
    @LuaInterface
    public static boolean canAccessPetInventory(@NotNull Hero hero, @NotNull Mob pet) {
        if (!pet.isAlive()) {
            return false;
        }
        if (pet.getOwnerId() != hero.getId()) {
            return false;
        }
        // Allow access if adjacent or on same cell
        return pet.adjacent(hero) || pet.getPos() == hero.getPos();
    }

    /**
     * Gives an item from hero's backpack to pet's backpack.
     */
    @LuaInterface
    public static boolean giveItemToPet(@NotNull Hero hero, @NotNull Mob pet, @NotNull Item item) {
        if (!canAccessPetInventory(hero, pet)) {
            GLog.w(StringsManager.getVar(R.string.PetInventory_TooFar));
            return false;
        }

        if (!hero.getBelongings().backpack.contains(item)) {
            return false;
        }

        if (item instanceof EquipableItem && ((EquipableItem) item).isEquipped(hero)) {
            GLog.w(StringsManager.getVar(R.string.PetInventory_CantGiveEquipped));
            return false;
        }

        // Check if pet's backpack has space
        if (pet.getBelongings().isBackpackFull()) {
            GLog.w(StringsManager.getVar(R.string.PetInventory_PetBackpackFull));
            return false;
        }

        // Detach from hero's backpack
        Item detached = item.detach(hero.getBelongings().backpack);
        if (detached == null) {
            return false;
        }

        // Collect into pet's backpack
        if (!detached.collect(pet.getBelongings().backpack)) {
            // If failed, put back in hero's backpack
            detached.collect(hero.getBelongings().backpack);
            return false;
        }

        // Update item owner to pet
        detached.setOwner(pet);

        GLog.i(StringsManager.getVar(R.string.PetInventory_GaveItem), item.name(), pet.getName());
        pet.updateSprite();
        // Refresh hero's bag UI if open
        if (WndBag.getHeroBagInstance() != null) {
            WndBag.getHeroBagInstance().updateItems();
        }
        // Refresh pet inventory UI if open
        WndPetBag.refreshCurrent();
        return true;
    }

    /**
     * Takes an item from pet's backpack to hero's backpack.
     */
    @LuaInterface
    public static boolean takeItemFromPet(@NotNull Hero hero, @NotNull Mob pet, @NotNull Item item) {
        if (!canAccessPetInventory(hero, pet)) {
            GLog.w(StringsManager.getVar(R.string.PetInventory_TooFar));
            return false;
        }

        if (!pet.getBelongings().backpack.contains(item)) {
            return false;
        }

        // Check if hero's backpack has space
        if (hero.getBelongings().isBackpackFull()) {
            GLog.w(StringsManager.getVar(R.string.PetInventory_HeroBackpackFull));
            return false;
        }

        // Detach from pet's backpack
        Item detached = item.detach(pet.getBelongings().backpack);
        if (detached == null) {
            return false;
        }

        // Collect into hero's backpack
        if (!detached.collect(hero.getBelongings().backpack)) {
            // If failed, put back in pet's backpack
            detached.collect(pet.getBelongings().backpack);
            return false;
        }

        // Update item owner to hero
        detached.setOwner(hero);

        GLog.i(StringsManager.getVar(R.string.PetInventory_TookItem), item.name(), pet.getName());
        pet.updateSprite();
        // Refresh hero's bag UI if open
        if (WndBag.getHeroBagInstance() != null) {
            WndBag.getHeroBagInstance().updateItems();
        }
        // Refresh pet inventory UI if open
        WndPetBag.refreshCurrent();
        return true;
    }

    /**
     * Equips an item on the pet.
     */
    @LuaInterface
    public static boolean equipItemOnPet(@NotNull Hero hero, @NotNull Mob pet, @NotNull EquipableItem item, @NotNull Belongings.Slot slot) {
        if (!canAccessPetInventory(hero, pet)) {
            GLog.w(StringsManager.getVar(R.string.PetInventory_TooFar));
            return false;
        }

        if (!pet.getBelongings().backpack.contains(item)) {
            return false;
        }

        // Check if pet has the required stats
        if (!item.statsRequirementsSatisfied()) {
            GLog.w(StringsManager.getVar(R.string.PetInventory_PetTooWeak), item.name());
            return false;
        }

        // Try to equip
        if (pet.getBelongings().equip(item, slot)) {
            GLog.i(StringsManager.getVar(R.string.PetInventory_EquippedOnPet), item.name(), pet.getName());
            pet.updateSprite();
            // Refresh pet inventory UI if open
            WndPetBag.refreshCurrent();
            return true;
        }

        return false;
    }

    /**
     * Unequips an item from the pet.
     /** Unequips an item from the pet and puts it in pet's backpack (or drops on ground if full). */
    @LuaInterface
    public static boolean unequipItemFromPet(@NotNull Mob pet, @NotNull EquipableItem item) {
        if (!pet.isAlive()) {
            return false;
        }

        if (!pet.getBelongings().isEquipped(item)) {
            return false;
        }

        if (item.isCursed()) {
            GLog.w(StringsManager.getVar(R.string.PetInventory_CursedItem), item.name());
            return false;
        }

        // Unequip the item (match hero behavior: collect=true drops on ground if full)
        if (pet.getBelongings().unequip(item)) {
            // Try to collect into pet's backpack
            if (!item.collect(pet.getBelongings().backpack)) {
                // If backpack full, drop on ground (matches hero behavior in doUnequip)
                item.doDrop(pet);
                GLog.i(StringsManager.getVar(R.string.PetInventory_UnequippedFromPet), item.name(), pet.getName());
                pet.updateSprite();
                // Refresh pet inventory UI if open
                WndPetBag.refreshCurrent();
                return true;
            }

            GLog.i(StringsManager.getVar(R.string.PetInventory_UnequippedFromPet), item.name(), pet.getName());
            pet.updateSprite();
            // Refresh pet inventory UI if open
            WndPetBag.refreshCurrent();
            return true;
        }

        return false;
    }

    /**
     * Opens the pet inventory window for the given pet.
     */
    @LuaInterface
    public static void openPetInventory(@NotNull Hero hero, @NotNull Mob pet) {
        if (!canAccessPetInventory(hero, pet)) {
            GLog.w(StringsManager.getVar(R.string.PetInventory_TooFar));
            return;
        }

        GameScene.show(new WndPetBag(hero, pet));
    }

    /**
     * Opens the pet selection window if hero has multiple pets.
     * If item is provided, it will be given to the selected pet.
     */
    @LuaInterface
    public static void openPetSelect(@NotNull Hero hero, @Nullable Item itemToGive) {
        GameScene.show(new WndPetSelect(hero, itemToGive));
    }

    /**
     * Opens the pet selection window if hero has multiple pets.
     */
    @LuaInterface
    public static void openPetSelect(@NotNull Hero hero) {
        openPetSelect(hero, null);
    }

    /**
     * Gets all pets owned by the hero.
     */
    @LuaInterface
    @NotNull
    public static java.util.List<Mob> getHeroPets(@NotNull Hero hero) {
        java.util.List<Mob> pets = new java.util.ArrayList<>();
        for (Mob mob : hero.level().mobs) {
            if (mob.getOwnerId() == hero.getId() && mob.isAlive()) {
                pets.add(mob);
            }
        }
        return pets;
    }

    /**
     * Checks if the hero has any pets.
     */
    @LuaInterface
    public static boolean hasPets(@NotNull Hero hero) {
        return !getHeroPets(hero).isEmpty();
    }

    /**
     * Checks if the hero has multiple pets.
     */
    @LuaInterface
    public static boolean hasMultiplePets(@NotNull Hero hero) {
        return getHeroPets(hero).size() > 1;
    }
}