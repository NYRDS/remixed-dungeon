/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.actors.hero;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.generated.BundleHelper;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.keys.IronKey;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lombok.var;

public class Belongings implements Iterable<Item>, Bundlable {

	public static final int BACKPACK_SIZE	= 18;
	public static final Belongings empty = new Belongings(CharsList.DUMMY);

	private Char owner;
	
	public Bag backpack;

	public enum Slot{
		NONE,
		WEAPON,
		LEFT_HAND,
		ARMOR,
		ARTIFACT,
		LEFT_ARTIFACT
	}

	public Map<Slot, EquipableItem> blockedSlots = new HashMap<>();
	public Map<EquipableItem, Slot> usedSlots    = new HashMap<>();

	private Set<EquipableItem> activatedItems = new HashSet<>();

	@Packable
	public EquipableItem weapon = null;
	@Packable
	public EquipableItem leftHand = null;
	@Packable
	public Armor         armor  = null;
	@Packable
	public EquipableItem ring1  = null;
	@Packable
	public EquipableItem ring2  = null;

	public Belongings( Char owner ) {
		this.owner = owner;
		
		backpack = new Backpack();
		backpack.owner = owner;

		collect(new Gold(0));
	}

	public void storeInBundle( Bundle bundle ) {
		backpack.storeInBundle(bundle);
		BundleHelper.Pack(this,bundle);
	}

	@Override
	public boolean dontPack() {
		return false;
	}

	public void restoreFromBundle( Bundle bundle ) {
		
		backpack.clear();
		backpack.restoreFromBundle(bundle);
		BundleHelper.UnPack(this,bundle);

		activateEquippedItems();
	}

	@LuaInterface
	public boolean slotBlocked(String slot) {
		Slot eSlot = Slot.valueOf(slot);
		return itemBySlot(eSlot) != null || blockedSlots.containsKey(eSlot);
	}

	private void blockSlots() {
		blockedSlots.clear();
		var itemIterator = iterator();

		while (itemIterator.hasNextEquipped()) {
			EquipableItem item = (EquipableItem) itemIterator.next();

			if(item!=null){
				blockedSlots.put(item.blockSlot(), item);
			}
		}
	}

	private void activateEquippedItems() {
		var itemIterator = iterator();

		while (itemIterator.hasNextEquipped()) {
			EquipableItem item = (EquipableItem) itemIterator.next();
			if(item!=null) {
				item.setOwner(owner);
				if (!activatedItems.contains(item)) {
					item.activate(owner);
					activatedItems.add(item);
				}
			}
		}
		blockSlots();
	}

	@LuaInterface
	public String itemSlotName(EquipableItem item) {
		if (usedSlots.containsKey(item)) {
			return usedSlots.get(item).name();
		}
		return Slot.NONE.name();
	}

	public boolean isEquipped(Item item) {
		return item.equals(weapon) || item.equals(armor) || item.equals(leftHand) || item.equals(ring1) || item.equals(ring2);
	}

	public Item checkItem( Item src ) {
		for (Item item : this) {
			if (item == src ) {
				return item;
			}
		}
		return null;
	}

	public Item getItem( String itemClass ) {
		for (Item item : this) {
			if (itemClass.equals( item.getClassName() )) {
				return item;
			}
		}
		return null;
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public<T extends Item> T getItem( Class<T> itemClass ) {

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				return (T)item;
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
				if (levelId.equals(key.levelId) || (Utils.UNKNOWN.equals(key.levelId) && key.depth == depth)) {
					return (T) item;
				}
			}
		}
		return null;
	}

	public void countIronKeys() {
		
		IronKey.curDepthQuantity = 0;
		
		for (Item item : backpack) {
			if (item instanceof IronKey && ((IronKey)item).levelId.equals(DungeonGenerator.getCurrentLevelId())) {
				IronKey.curDepthQuantity++;
			}
		}
	}
	
	public void identify() {
		for (Item item : this) {
			item.identify();
		}
	}
	
	public void observe() {
		var itemIterator = iterator();

		while (itemIterator.hasNextEquipped()) {
			EquipableItem item = (EquipableItem) itemIterator.next();
			item.identify();
			Badges.validateItemLevelAcquired(item);
		}

		for (Item item : backpack) {
			item.cursedKnown = true;
		}
	}
	
	public void uncurseEquipped() {
		ScrollOfRemoveCurse.uncurse( owner, armor, weapon, leftHand, ring1, ring2 );
	}

	public void curseEquipped() {
		ScrollOfCurse.curse( owner, armor, weapon, leftHand, ring1, ring2 );
	}

	public Item randomUnequipped() {
		return Random.element( backpack.items );
	}

	public boolean removeItem(Item itemToRemove) {
		usedSlots.remove(itemToRemove);
		itemToRemove.setOwner(CharsList.DUMMY);

		if(itemToRemove.equals(weapon)) {
			weapon = null;
			return true;
		}

		if(itemToRemove.equals(armor)) {
			armor = null;
			return true;
		}

		if(itemToRemove.equals(leftHand)) {
			leftHand = null;
			return true;
		}

		if(itemToRemove.equals(ring1)) {
			ring1 = null;
			return true;
		}

		if(itemToRemove.equals(ring2)) {
			ring2 = null;
			return true;
		}

		return backpack.remove(itemToRemove);
	}

	public void resurrect( int depth ) {

		for (Item item : backpack.items.toArray(new Item[0])) {
			if (item instanceof Key) {
				if (((Key) item).depth == depth) {
					item.detachAll(backpack);
				}
			} else if (item instanceof Amulet) {

			} else if (!item.isEquipped(owner)) {
				item.detachAll(backpack);
			}
		}

		uncurseEquipped();
		activateEquippedItems();
	}
	
	public int charge( boolean full) {
		
		int count = 0;
		
		for (Item item : this) {
			if (item instanceof Wand) {
				Wand wand = (Wand)item;
				if (wand.curCharges() < wand.maxCharges()) {
					wand.curCharges(full ? wand.maxCharges() : wand.curCharges() + 1);
					count++;

					QuickSlot.refresh();
				}
			}
		}
		
		return count;
	}
	
	public void discharge() {
		for (Item item : this) {
			if (item instanceof Wand) {
				Wand wand = (Wand)item;
				if (wand.curCharges() > 0) {
					wand.curCharges(wand.curCharges() - 1);
					QuickSlot.refresh();
				}
			}
		}
	}

	void setupFromJson(@NotNull JSONObject desc) throws JSONException {
		try {
			if (desc.has("armor")) {
				armor = (Armor) ItemFactory.createItemFromDesc(desc.getJSONObject("armor"));
			}

			if (desc.has("weapon")) {
				weapon = (EquipableItem) ItemFactory.createItemFromDesc(desc.getJSONObject("weapon"));
			}

			if (desc.has("left_hand")) {
				leftHand = (EquipableItem) ItemFactory.createItemFromDesc(desc.getJSONObject("left_hand"));
			}

			if (desc.has("ring1")) {
				ring1 = (EquipableItem) ItemFactory.createItemFromDesc(desc.getJSONObject("ring1"));
			}

			if (desc.has("ring2")) {
				ring2 = (EquipableItem) ItemFactory.createItemFromDesc(desc.getJSONObject("ring2"));
			}
		} catch (ClassCastException e) {
			throw ModdingMode.modException(e);
		}

		if (desc.has("items")) {
			JSONArray items = desc.getJSONArray("items");
			for (int i = 0; i < items.length(); ++i) {
				Item item = ItemFactory.createItemFromDesc(items.getJSONObject(i));
				if(item!=null) {
					collect(item);
				}
			}
		}

		activateEquippedItems();
	}

	@Override
	@NotNull
	public ItemIterator iterator() {
		return new ItemIterator(); 
	}

	public Object getOwner() {
		return owner;
	}

	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		
		private Iterator<Item> backpackIterator = backpack.iterator();
		
		private Item[] equipped = {weapon, armor, leftHand, ring1, ring2};

		public boolean hasNextEquipped(){
			for (int i = index; i < equipped.length; i++) {
				if (equipped[i] != null) {
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
				if (item != null) {
					return item;
				}
			}
			index++;
			return backpackIterator.next();
		}
	}

	public boolean collect(Item newItem) {
		if(owner instanceof Hero) {
			return newItem.collect(backpack);
		}

		if (backpack.items.contains(newItem)) {
			return true;
		}

		if (newItem.stackable) {
			String c = newItem.getClassName();
			for (Item item : backpack.items) {
				if (item.getClassName().equals(c) && item.level() == newItem.level()) {
					item.quantity(item.quantity() + newItem.quantity());
					return true;
				}
			}
		}

		backpack.items.add(newItem);

		return true;
	}
	
	@Contract(pure = true)
	public static int getBackpackSize(){
		return BACKPACK_SIZE;
	}

	public boolean unequip(EquipableItem item) {
		if(checkItem(item)!=null) {
			item.deactivate(owner);
			removeItem(item);
			activatedItems.remove(item);
			blockSlots();
			owner.updateSprite();
			return true;
		}

		return false;
	}

	public Item itemBySlot(Belongings.Slot slot) {
		switch (slot) {
			case NONE:
				return null;
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
		return null;
	}

	public boolean equip(@NotNull EquipableItem item, Slot slot) {
		if(slot==Slot.NONE) {
			return false;
		}

		Item blockingItem = blockedSlots.get(slot);

		if(blockingItem==null) {
			blockingItem = itemBySlot(item.blockSlot());
		}

		if(blockingItem!=null) {
			GLog.w(Game.getVar(R.string.Belongings_CantWearBoth),
					item.name(),
					blockingItem.name());
			return false;
		}

		if(slot==Slot.WEAPON) {
			if (weapon == null || weapon.doUnequip( owner, true )) {
				weapon = (EquipableItem) item.detach(backpack);
			} else {
				return false;
			}
		}

		if(slot==Slot.ARMOR) {
			if (armor == null || armor.doUnequip( owner, true)) {
				armor = (Armor) item.detach(backpack);
			} else {
				return false;
			}
		}

		if(slot==Slot.LEFT_HAND) {
			if (leftHand == null || leftHand.doUnequip( owner, true)) {
				leftHand = (EquipableItem) item.detach(backpack);
			} else {
				return false;
			}
		}

		if(slot==Slot.ARTIFACT) {
			if (ring1 != null && ring2 != null) {
				GLog.w(Game.getVar(R.string.Artifact_Limit));
				return false;
			} else {
				if (ring1 == null) {
					ring1 = (EquipableItem) item.detach(backpack);;
				} else {
					ring2 = (EquipableItem) item.detach(backpack);;
				}
			}
		}

		item.cursedKnown = true;
		if(item.cursed) {
			ItemUtils.equipCursed( owner );
			item.equippedCursed();
		}

		usedSlots.put(item, slot);

		activateEquippedItems();
		blockSlots();
		QuickSlot.refresh();
		owner.updateSprite();

		owner.spendAndNext(item.time2equip(owner));

		return true;
	}

}
