
package com.watabou.pixeldungeon.actors.hero;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.generated.BundleHelper;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.keys.Key;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfCurse;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



public class Belongings implements Iterable<Item>, Bundlable {

    public static final int BACKPACK_SIZE = 18;

    private Item selectedItem = ItemsList.DUMMY;

    private final Char owner;

    public Bag backpack;

    public Item getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(@NotNull Item selectedItem) {
        this.selectedItem = selectedItem;
    }

    @NotNull
    public EquipableItem getItemFromSlot(@NotNull Slot slot) {
        switch (slot) {
            case NONE:
                return ItemsList.DUMMY;
            case WEAPON:
                return weapon;
            case LEFT_HAND:
                return leftHand;
            case ARMOR:
                return armor;
            case ARTIFACT:
                return ring1;
            case LEFT_ARTIFACT:
                return ring2;
        }
        return ItemsList.DUMMY;
    }

    @LuaInterface
    @TestOnly
    public Item randomEquipped() {
        return getItemFromSlot(Random.oneOf(Slot.values()));
    }

    public void setItemForSlot(EquipableItem item, Slot slot) {
        usedSlots.put(item, slot);
        item.setOwner(owner);

        if (!activatedItems.contains(item)) {
            activatedItems.add(item);
            item.activate(owner);
        }
        blockedSlots.put(item.blockSlot(), item);

        switch (slot) {
            case NONE:
                break;
            case WEAPON:
                weapon = item;
                break;
            case LEFT_HAND:
                leftHand = item;
                break;
            case ARMOR:
                armor = item;
                break;
            case ARTIFACT:
                ring1 = item;
                break;
            case LEFT_ARTIFACT:
                ring2 = item;
                break;
        }
    }

    public enum Slot {
        NONE,
        WEAPON,
        LEFT_HAND,
        ARMOR,
        ARTIFACT,
        LEFT_ARTIFACT
    }

    private final Map<Slot, EquipableItem> blockedSlots = new HashMap<>();
    public Map<EquipableItem, Slot> usedSlots = new HashMap<>();

    private final Set<EquipableItem> activatedItems = new HashSet<>();

    @Packable(defaultValue = "ItemsList.DUMMY")
    @NotNull
    public EquipableItem weapon = ItemsList.DUMMY;
    @Packable(defaultValue = "ItemsList.DUMMY")
    @NotNull
    public EquipableItem leftHand = ItemsList.DUMMY;
    @Packable(defaultValue = "ItemsList.DUMMY")
    @NotNull
    public EquipableItem armor = ItemsList.DUMMY;
    @Packable(defaultValue = "ItemsList.DUMMY")
    @NotNull
    public EquipableItem ring1 = ItemsList.DUMMY;
    @Packable(defaultValue = "ItemsList.DUMMY")
    @NotNull
    public EquipableItem ring2 = ItemsList.DUMMY;

    public Belongings(Char owner) {
        this.owner = owner;

        backpack = new Backpack();
        backpack.setOwner(owner);

        collect(new Gold(0));
    }

    public void storeInBundle(Bundle bundle) {
        backpack.storeInBundle(bundle);
        BundleHelper.Pack(this, bundle);
    }

    @Override
    public boolean dontPack() {
        return false;
    }

    public void restoreFromBundle(Bundle bundle) {
        backpack.clear();
        backpack.restoreFromBundle(bundle);
        BundleHelper.UnPack(this, bundle);

        for (Slot slot : Slot.values()) { //activate equipped items
            setItemForSlot(getItemFromSlot(slot), slot);
        }
    }

    @LuaInterface
    public boolean slotBlocked(String slot) {
        return slotBlocked(Slot.valueOf(slot));
    }

    public boolean slotBlocked(Slot slot) {
        return itemBySlot(slot) != ItemsList.DUMMY || blockedSlots.containsKey(slot);
    }

    @NotNull
    @LuaInterface
    public String itemSlotName(Item item) {
        if (usedSlots.containsKey(item)) {
            return usedSlots.get(item).name();
        }
        return Slot.NONE.name();
    }

    @NotNull
    public Slot itemSlot(Item item) {
        if (usedSlots.containsKey(item)) {
            return usedSlots.get(item);
        }
        return Slot.NONE;
    }

    public boolean isEquipped(@NotNull Item item) {
        return item.equals(getItemFromSlot(Slot.WEAPON)) || item.equals(getItemFromSlot(Slot.ARMOR)) || item.equals(getItemFromSlot(Slot.LEFT_HAND)) || item.equals(getItemFromSlot(Slot.ARTIFACT)) || item.equals(getItemFromSlot(Slot.LEFT_ARTIFACT));
    }

    @LuaInterface
    public Item checkItem(Item src) {
        for (Item item : this) {
            if (item == src) {
                return item;
            }
        }

        return ItemsList.DUMMY;
    }

    @LuaInterface
    @Nullable
    public Item getItem(String itemClass) { //Still here for old mods
        for (Item item : this) {
            if (itemClass.equals(item.getEntityKind())) {
                return item;
            }
        }
        return null;
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public <T extends Item> T getItem(Class<T> itemClass) {

        for (Item item : this) {
            if (itemClass.isInstance(item)) {
                return (T) item;
            }
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Key> T getKey(Class<T> kind, int depth, @NotNull String levelId) {
        for (Item item : backpack) {
            if (item instanceof Key && item.getClass() == kind) {
                Key key = (Key) item;
                if (levelId.equals(key.levelId)
                        || (Utils.UNKNOWN.equals(key.levelId) && key.getDepth() == depth)) {
                    return (T) key;
                }
            }
        }
        return null;
    }

    public void identify() {
        for (Item item : this) {
            item.identify();
        }
    }

    public Item encumbranceCheck() {
        var itemIterator = iterator();

        while (itemIterator.hasNextEquipped()) {
            EquipableItem item = (EquipableItem) itemIterator.next();
            if (item.requiredSTR() > owner.effectiveSTR()) {
                return item;
            }
        }
        return ItemsList.DUMMY;
    }

    public void observe() {
        var itemIterator = iterator();

        while (itemIterator.hasNextEquipped()) {
            EquipableItem item = (EquipableItem) itemIterator.next();
            item.identify();
            Badges.validateItemLevelAcquired(item);
        }

        for (Item item : backpack) {
            item.setCursedKnown(true);
        }
    }

    public void uncurseEquipped() {
        ScrollOfRemoveCurse.uncurse(owner, getItemFromSlot(Slot.ARMOR), getItemFromSlot(Slot.WEAPON), getItemFromSlot(Slot.LEFT_HAND), getItemFromSlot(Slot.ARTIFACT), getItemFromSlot(Slot.LEFT_ARTIFACT));
    }

    public void curseEquipped() {
        ScrollOfCurse.curse(owner, getItemFromSlot(Slot.ARMOR), getItemFromSlot(Slot.WEAPON), getItemFromSlot(Slot.LEFT_HAND), getItemFromSlot(Slot.ARTIFACT), getItemFromSlot(Slot.LEFT_ARTIFACT));
    }

    @NotNull
    public Item randomUnequipped() {
        Item ret = Random.element(backpack.items);
        if (ret == null) {
            return ItemsList.DUMMY;
        }
        return ret;
    }

    public boolean removeItem(Item itemToRemove) {
        if (itemToRemove instanceof EquipableItem && isEquipped(itemToRemove)) {
            var eItem = (EquipableItem) itemToRemove;
            eItem.deactivate(owner);
            usedSlots.remove(eItem);
        }

        itemToRemove.setOwner(CharsList.DUMMY);

        if (itemToRemove.equals(getItemFromSlot(Slot.WEAPON))) {
            setItemForSlot(ItemsList.DUMMY, Slot.WEAPON);
            return true;
        }

        if (itemToRemove.equals(getItemFromSlot(Slot.ARMOR))) {
            setItemForSlot(ItemsList.DUMMY, Slot.ARMOR);
            return true;
        }

        if (itemToRemove.equals(getItemFromSlot(Slot.LEFT_HAND))) {
            setItemForSlot(ItemsList.DUMMY, Slot.LEFT_HAND);
            return true;
        }

        if (itemToRemove.equals(getItemFromSlot(Slot.ARTIFACT))) {
            setItemForSlot(ItemsList.DUMMY, Slot.ARTIFACT);
            this.ring1 = ItemsList.DUMMY;
            return true;
        }

        if (itemToRemove.equals(getItemFromSlot(Slot.LEFT_ARTIFACT))) {
            setItemForSlot(ItemsList.DUMMY, Slot.LEFT_ARTIFACT);
            return true;
        }

        return backpack.remove(itemToRemove);
    }

    public int charge(boolean full) {

        int count = 0;

        for (Item item : this) {
            if (item instanceof Wand) {
                Wand wand = (Wand) item;
                if (wand.curCharges() < wand.maxCharges()) {
                    wand.curCharges(full ? wand.maxCharges() : wand.curCharges() + 1);
                    count++;

                    QuickSlot.refresh(owner);
                }
            }
        }

        return count;
    }

    public void discharge() {
        for (Item item : this) {
            if (item instanceof Wand) {
                Wand wand = (Wand) item;
                if (wand.curCharges() > 0) {
                    wand.curCharges(wand.curCharges() - 1);
                    QuickSlot.refresh(owner);
                }
            }
        }
    }

    public void setupFromJson(@NotNull JSONObject desc) throws JSONException {
        try {
            Item item;
            if (desc.has("armor")) {
                item = ItemFactory.createItemFromDesc(desc.getJSONObject("armor"));
                if (item instanceof EquipableItem) {
                    setItemForSlot((EquipableItem) item, Slot.ARMOR);
                } else {
                    collect(item);
                }
            }

            if (desc.has("weapon")) {
                item = ItemFactory.createItemFromDesc(desc.getJSONObject("weapon"));
                if (item instanceof EquipableItem) {
                    setItemForSlot((EquipableItem) item, Slot.WEAPON);
                } else {
                    collect(item);
                }
            }

            if (desc.has("left_hand")) {
                item = ItemFactory.createItemFromDesc(desc.getJSONObject("left_hand"));
                if (item instanceof EquipableItem) {
                    setItemForSlot((EquipableItem) item, Slot.LEFT_HAND);
                } else {
                    collect(item);
                }
            }

            if (desc.has("ring1")) {
                item = ItemFactory.createItemFromDesc(desc.getJSONObject("ring1"));
                if (item instanceof EquipableItem) {
                    setItemForSlot((EquipableItem) item, Slot.ARTIFACT);
                } else {
                    collect(item);
                }
            }

            if (desc.has("ring2")) {
                item = ItemFactory.createItemFromDesc(desc.getJSONObject("ring2"));
                if (item instanceof EquipableItem) {
                    setItemForSlot((EquipableItem) item, Slot.LEFT_ARTIFACT);
                } else {
                    collect(item);
                }
            }
        } catch (ClassCastException e) {
            throw ModdingMode.modException(e);
        }

        if (desc.has("items")) {
            JSONArray items = desc.getJSONArray("items");
            for (int i = 0; i < items.length(); ++i) {
                Item item = ItemFactory.createItemFromDesc(items.getJSONObject(i));
                collect(item);
            }
        }
    }

    @Override
    @NotNull
    public ItemIterator iterator() {
        return new ItemIterator();
    }

    @LuaInterface
    @NotNull
    public Char getOwner() {
        return owner;
    }

    private class ItemIterator implements Iterator<Item> {

        private int index = 0;

        private final Iterator<Item> backpackIterator = backpack.iterator();

        private final EquipableItem[] equipped = {getItemFromSlot(Slot.WEAPON), getItemFromSlot(Slot.ARMOR), getItemFromSlot(Slot.LEFT_HAND), getItemFromSlot(Slot.ARTIFACT), getItemFromSlot(Slot.LEFT_ARTIFACT)};

        public boolean hasNextEquipped() {
            for (int i = index; i < equipped.length; i++) {
                if (equipped[i].valid()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean hasNext() {
            return hasNextEquipped() || backpackIterator.hasNext();
        }

        @Override
        public Item next() {
            while (index < equipped.length) {
                Item item = equipped[index++];
                if (item != ItemsList.DUMMY && item != null) {
                    return item;
                }
            }
            index++;
            return backpackIterator.next();
        }
    }

    public boolean collect(@NotNull Item newItem) {
        return newItem.collect(backpack);
    }

    @Contract(pure = true)
    public static int getBackpackSize() {
        return BACKPACK_SIZE;
    }

    public boolean unequip(EquipableItem item) {
        if (checkItem(item).valid()) {
            removeItem(item);
            activatedItems.remove(item);
            blockedSlots.remove(item.blockSlot());
            owner.updateSprite();
            return true;
        }

        return false;
    }

    public boolean drop(EquipableItem item) {
        if (unequip(item)) {
            item.doDrop(owner);
            return true;
        }
        return false;
    }

    public void dropAll() {
        var itemsToDrop = new ArrayList<Item>();

        for (Item item : this) {
            if (item.quantity() > 0) {
                itemsToDrop.add(item);
            }
        }

        for (Item item : itemsToDrop) {
            item.doDrop(owner);
        }
    }

    public boolean isBackpackEmpty() {
        for (Item item : backpack) {
            if (item.quantity() > 0) {
                return false;
            }
        }
        return true;
    }

    @LuaInterface
    public boolean isBackpackFull() {
        return backpack.items.size() == getBackpackSize();
    }

    @NotNull
    public Item itemBySlot(Belongings.Slot slot) {
        switch (slot) {
            case WEAPON:
                return getItemFromSlot(Slot.WEAPON);
            case LEFT_HAND:
                return getItemFromSlot(Slot.LEFT_HAND);
            case ARMOR:
                return getItemFromSlot(Slot.ARMOR);
            case ARTIFACT:
                return getItemFromSlot(Slot.ARTIFACT);
            case LEFT_ARTIFACT:
                return getItemFromSlot(Slot.LEFT_ARTIFACT);
        }
        return ItemsList.DUMMY;
    }

    public boolean equip(@NotNull EquipableItem item, Slot slot) {
        if (slot == Slot.NONE) {
            return false;
        }

        Item blockingItem = blockedSlots.get(slot);

        if (blockingItem == null) {
            blockingItem = itemBySlot(item.blockSlot());
        }

        if (blockingItem != ItemsList.DUMMY) {
            GLog.w(StringsManager.getVar(R.string.Belongings_CantWearBoth),
                    item.name(),
                    blockingItem.name());
            return false;
        }

        if (slot == Slot.WEAPON) {
            if (getItemFromSlot(Slot.WEAPON).doUnequip(owner, true)) {
                setItemForSlot((EquipableItem) item.detach(backpack), Slot.WEAPON);
            } else {
                return false;
            }
        }

        if (slot == Slot.ARMOR) {
            if (getItemFromSlot(Slot.ARMOR).doUnequip(owner, true)) {
                setItemForSlot((EquipableItem) item.detach(backpack), Slot.ARMOR);
            } else {
                return false;
            }
        }

        if (slot == Slot.LEFT_HAND) {
            if (getItemFromSlot(Slot.LEFT_HAND).doUnequip(owner, true)) {
                setItemForSlot((EquipableItem) item.detach(backpack), Slot.LEFT_HAND);
            } else {
                return false;
            }
        }

        if (slot == Slot.ARTIFACT || slot == Slot.LEFT_ARTIFACT) {
            if (getItemFromSlot(Slot.ARTIFACT) != ItemsList.DUMMY && getItemFromSlot(Slot.LEFT_ARTIFACT) != ItemsList.DUMMY) {
                GLog.w(StringsManager.getVar(R.string.Artifact_Limit));
                return false;
            } else {
                if (getItemFromSlot(Slot.ARTIFACT) == ItemsList.DUMMY) {
                    EquipableItem ring11 = (EquipableItem) item.detach(backpack);
                    setItemForSlot(ring11, Slot.ARTIFACT);
                    this.ring1 = ring11;
                } else {
                    setItemForSlot((EquipableItem) item.detach(backpack), Slot.LEFT_ARTIFACT);
                }
            }
        }

        item.setCursedKnown(true);
        if (item.isCursed()) {
            ItemUtils.equipCursed(owner);
            item.equippedCursed();
        }

        QuickSlot.refresh(owner);
        owner.updateSprite();

        owner.spend(item.time2equip(owner));

        return true;
    }

}
