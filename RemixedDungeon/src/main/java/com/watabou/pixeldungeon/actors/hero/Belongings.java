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

import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.KindOfWeapon;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.keys.IronKey;
import com.watabou.pixeldungeon.items.keys.Key;
import com.watabou.pixeldungeon.items.rings.Artifact;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Belongings implements Iterable<Item> {

	public static final int BACKPACK_SIZE	= 19;
	
	private Char owner;
	
	public Bag backpack;	

	public KindOfWeapon weapon = null;
	public Armor        armor  = null;
	public Artifact     ring1  = null;
	public Artifact     ring2  = null;

	@NonNull
	public Gold         gold   = new Gold(0);

	public Belongings( Char owner ) {
		this.owner = owner;
		
		backpack = new Backpack();
		backpack.owner = owner;
	}
	
	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String RING1		= "ring1";
	private static final String RING2		= "ring2";
	private static final String GOLD		= "gold";
	
	public void storeInBundle( Bundle bundle ) {
		
		backpack.storeInBundle(bundle);
		
		bundle.put( WEAPON, weapon );
		bundle.put( ARMOR, armor );
		bundle.put( RING1, ring1 );
		bundle.put( RING2, ring2 );
		bundle.put( GOLD,  gold );
	}
	
	public void restoreFromBundle( Bundle bundle ) {
		
		backpack.clear();
		backpack.restoreFromBundle(bundle);
		
		weapon = (KindOfWeapon)bundle.get( WEAPON );
		if (weapon != null) {
			weapon.activate( owner );
		}
		
		armor = (Armor)bundle.get( ARMOR );
		
		ring1 = (Artifact)bundle.get( RING1 );
		if (ring1 != null) {
			ring1.activate( owner );
		}
		
		ring2 = (Artifact)bundle.get( RING2 );
		if (ring2 != null) {
			ring2.activate( owner );
		}

		Gold storedGold = (Gold) bundle.get(GOLD);
		//pre 28.5 saves compatibility
		gold = storedGold != null ? storedGold : gold;
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
	public <T extends Key> T getKey(Class<T> kind, int depth, @NonNull String levelId) {
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
		if (weapon != null) {
			weapon.identify();
			Badges.validateItemLevelAcquired( weapon );
		}
		if (armor != null) {
			armor.identify();
			Badges.validateItemLevelAcquired( armor );
		}
		if (ring1 != null) {
			ring1.identify();
			Badges.validateItemLevelAcquired( ring1 );
		}
		if (ring2 != null) {
			ring2.identify();
			Badges.validateItemLevelAcquired( ring2 );
		}
		for (Item item : backpack) {
			item.cursedKnown = true;
		}
	}
	
	public void uncurseEquipped() {
		ScrollOfRemoveCurse.uncurse( owner, armor, weapon, ring1, ring2 );
	}
	
	public Item randomUnequipped() {
		return Random.element( backpack.items );
	}

	public boolean removeItem(Item itemToRemove) {
		Iterator<Item> it = iterator();

		while(it.hasNext()) {
			Item item = it.next();
			if(item == itemToRemove) {
				it.remove();
				return true;
			}
		}
		return false;
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
		
		if (weapon != null) {
			weapon.cursed = false;
			weapon.activate( owner );
		}
		
		if (armor != null) {
			armor.cursed = false;
		}
		
		if (ring1 != null) {
			ring1.cursed = false;
			ring1.activate( owner );
		}
		if (ring2 != null) {
			ring2.cursed = false;
			ring2.activate( owner );
		}
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


	void setupFromJson(JSONObject desc) throws JSONException {
		if (desc.has("armor")) {
			armor = (Armor) ItemFactory.createItemFromDesc(desc.getJSONObject("armor"));
		}

		if (desc.has("weapon")) {
			weapon = (KindOfWeapon) ItemFactory.createItemFromDesc(desc.getJSONObject("weapon"));
		}

		if (desc.has("ring1")) {
			ring1 = (Artifact) ItemFactory.createItemFromDesc(desc.getJSONObject("ring1"));
		}

		if (desc.has("ring2")) {
			ring2 = (Artifact) ItemFactory.createItemFromDesc(desc.getJSONObject("ring2"));
		}

		if (desc.has("items")) {
			JSONArray items = desc.getJSONArray("items");
			for (int i = 0; i < items.length(); ++i) {
				ItemFactory.createItemFromDesc(items.getJSONObject(i)).collect(backpack);
			}
		}
	}

	@Override
	@NonNull
	public Iterator<Item> iterator() {
		return new ItemIterator(); 
	}

	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		
		private Iterator<Item> backpackIterator = backpack.iterator();
		
		private Item[] equipped = {weapon, armor, ring1, ring2};
		private int backpackIndex = equipped.length;
		
		@Override
		public boolean hasNext() {
			
			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}
			
			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {
			while (index < backpackIndex) {
				Item item = equipped[index++];
				if (item != null) {
					return item;
				}
			}
			index++;
			return backpackIterator.next();
		}

		@Override
		public void remove() {
			if(index == 0) {
				throw new IllegalStateException();
			}
			switch (index-1) {
			case 0:
				equipped[0] = weapon = null;
				break;
			case 1:
				equipped[1] = armor = null;
				break;
			case 2:
				equipped[2] = ring1 = null;
				break;
			case 3:
				equipped[3] = ring2 = null;
				break;
			default:
				backpackIterator.remove();
			}
		}
	}

	public static int getBackpackSize(){
		return BACKPACK_SIZE;
	}

}
