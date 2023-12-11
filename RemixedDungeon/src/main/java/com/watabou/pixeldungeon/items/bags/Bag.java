
package com.watabou.pixeldungeon.items.bags;

import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

public class Bag extends Item implements Iterable<Item> {

	public static final String AC_OPEN = "Bag_ACOpen";

	public static final String KEYRING = "Keyring";
	
	{
		image = 11;
		
		setDefaultAction(AC_OPEN);
	}

	public ArrayList<Item> items = new ArrayList<>();
		
	@Override
	public ArrayList<String> actions(Char hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add(AC_OPEN);
		return actions;
	}
	
	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		if (action.equals( AC_OPEN )) {
			GameScene.show( new WndBag(chr.getBelongings(), this, null, WndBag.Mode.ALL, null ) );
		} else {
			super._execute(chr, action );
		}
	}
	
	@Override
	public boolean collect(@NotNull Bag container ) {
		if (super.collect( container )) {	

			for(Item item : items) {
				item.setOwner(getOwner());
			}

			for (Item item : container.items.toArray(new Item[0])) {
				if (grab( item )) {
					item.detachAll( container );
					item.collect( this );
				}
			}

			if(getOwner() == Dungeon.hero) {
				Badges.validateAllBagsBought(this);
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void onDetach( ) {

		for (Item item : items.toArray(new Item[0])) {
			if (grab( item )) {
				item.detachAll( this );
				if(getOwner().isAlive()) {
					getOwner().collect(item);
				}
			}
		}

		super.onDetach();
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	public void clear() {
		items.clear();
	}
	
	private static final String ITEMS	= "inventory";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ITEMS, items );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		for (Item item : bundle.getCollection( ITEMS, Item.class )) {
			item.collect(this);
		}
	}
	
	public boolean contains( Item item ) {
		for (Item i : items) {
			if (i == item) {
				return true;
			} else if (i instanceof Bag && ((Bag)i).contains( item )) {
				return true;
			}
		}
		return false;
	}
	
	public boolean grab(@NotNull Item item ) {
        return getOwner().useBags() && item.bag().equals(getEntityKind());
	}

	@Override
	public String desc() {
		return Utils.format(super.desc(), getSize());
	}


	public boolean remove(Item item) {
		for(Item i:items) {
			if(i instanceof Bag) {
				if(((Bag)i).remove(item)) {
					return true;
				}
			}
		}

		return items.remove(item);
	}

	@NotNull
	@Override
	public BagIterator iterator() {
		return new BagIterator();
	}

	public int getSize() {
		return getOwner() instanceof Hero ? Belongings.getBackpackSize() : Belongings.getBackpackSize() + 1;
	}


	private class BagIterator implements Iterator<Item> {

		private int index = 0;
		private BagIterator nested = null;

		@Override
		public boolean hasNext() {
			if (nested != null) {
				return nested.hasNext() || index < items.size();
			} else {
				return index < items.size();
			}
		}

		@Override
		public Item next() {

			if (nested != null && nested.hasNext()) {
				return nested.next();
			} else {
				
				nested = null;

				Item item = items.get( index++ );
				if (item instanceof Bag) {
					Bag bag = (Bag)item;
					if(!bag.items.isEmpty()) {
						nested = ((Bag) item).iterator();
					}
				}
				
				return item;
			}
		}

		@Override
		public void remove() {
			if (nested != null) {
				nested.remove();
			} else {
				items.remove( index - 1 );
			}
		}
	}

	@Override
	public String getEntityKind() {
		return getClass().getSimpleName();
	}
}
