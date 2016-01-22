package com.nyrds.pixeldungeon.items.common;

import java.util.HashMap;

import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Random;


public class ItemFactory {

	static private HashMap <String, Class<? extends Item>> mItemsList;
	
	private static void registerItemClass(Class<? extends Item> itemClass) {
		mItemsList.put(itemClass.getSimpleName(), itemClass);
	}
	
	private static void initItemsMap() {
		
		mItemsList = new HashMap<>();
	}
	
	public static Class<? extends Item> itemClassRandom() {
		if(mItemsList==null) {
			initItemsMap();
		}
		
		return Random.element(mItemsList.values());
	}
	
	public static Class<? extends Item> itemsClassByName(String selectedItemClass) {
		
		if(mItemsList==null) {
			initItemsMap();
		}
		
		Class<? extends Item> itemClass = mItemsList.get(selectedItemClass);
		if(itemClass != null) {
			return itemClass;
		} else {
			Game.toast("Unknown iten: [%s], spawning Gold instead",selectedItemClass);
			return Gold.class;
		}
	}

}
