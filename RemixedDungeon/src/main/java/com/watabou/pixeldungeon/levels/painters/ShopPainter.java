
package com.watabou.pixeldungeon.levels.painters;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfIdentify;
import com.watabou.pixeldungeon.levels.LastShopLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.Random;

public class ShopPainter extends Painter {

	public static void paint( Level level, Room room ) {
		
		fill( level, room, Terrain.WALL );
		fill( level, room, 1, Terrain.EMPTY_SP );
		
		pasWidth = room.width() - 2;
		pasHeight = room.height() - 2;

		placeShopkeeper( level, room );
		
		for (Room.Door door : room.connected.values()) {
			door.set( Room.Door.Type.REGULAR );
		}
	}

	private static void placeShopkeeper( Level level, Room room ) {

		int pos;
		do {
			pos = room.random(level);
		} while (level.getHeap( pos ) != null);


		// data-defined shopkeepers (batch 16c): Imp at the last shop,
		// Azuterron at d27, plain Shopkeeper elsewhere
		Mob shopkeeper;
		if (level instanceof LastShopLevel) {
			shopkeeper = MobFactory.mobByName( MobFactory.IMP_SHOPKEEPER );
		} else if (Dungeon.depth == 27) {
			shopkeeper = MobFactory.mobByName( MobFactory.AZUTERRON );
		} else {
			shopkeeper = MobFactory.mobByName( MobFactory.SHOPKEEPER );
		}
		shopkeeper.setPos(pos);

		switch (Dungeon.depth) {

			case 6:
				stock( level, shopkeeper, kmd("Quarterstaff" , "Spear") );
				stock( level, shopkeeper, ItemFactory.itemByName("Weightstone") );
				break;

			case 11:
				stock( level, shopkeeper, kmd("Sword", "Mace") );
				stock( level, shopkeeper, ItemFactory.itemByName("Weightstone") );
				break;

			case 16:
				stock( level, shopkeeper, kmd("Longsword", "BattleAxe") );
				stock( level, shopkeeper, ItemFactory.itemByName("Weightstone") );
				break;

			case 21:
				switch (Random.Int( 3 )) {
					case 0:
						stock( level, shopkeeper, kmd1("Glaive") );
						break;
					case 1:
						stock( level, shopkeeper, kmd1("WarHammer") );
						break;
					case 2:
						break;
				}
				stock( level, shopkeeper, ItemFactory.itemByName("Weightstone") );
				stock( level, shopkeeper, ItemFactory.itemByName("Torch") );
				stock( level, shopkeeper, ItemFactory.itemByName("Torch") );
				break;

			case 27:
				switch (Random.Int( 3 )) {
					case 0:
						stock( level, shopkeeper, kmd1("Claymore") );
						break;
					case 1:
						stock( level, shopkeeper, kmd1("Halberd") );
						break;
					case 2:
						break;
				}
				stock( level, shopkeeper, ItemFactory.itemByName(Potion.HEALING) );
				stock( level, shopkeeper, ItemFactory.itemByName(Potion.EXPERIENCE));
				stock( level, shopkeeper, ItemFactory.itemByName(Potion.MIGHT));
				break;
		}

		stock( level, shopkeeper, ItemFactory.itemByName(Potion.HEALING) );
		for (int i=0; i < 2; i++) {
			stock( level, shopkeeper, Treasury.getLevelTreasury().random( Treasury.Category.POTION ) );
		}

		stock( level, shopkeeper, new ScrollOfIdentify() );
		stock( level, shopkeeper, ItemFactory.itemByName(Scroll.REMOVE_CURSE) );
		stock( level, shopkeeper, ItemFactory.itemByName(Scroll.MAGIC_MAPPING) );
		stock( level, shopkeeper, Treasury.getLevelTreasury().random( Treasury.Category.SCROLL ) );

		stock( level, shopkeeper, ItemFactory.itemByName(Food.OVERPRICED_RATION) );
		stock( level, shopkeeper, ItemFactory.itemByName(Food.OVERPRICED_RATION) );

		stock( level, shopkeeper, ItemFactory.itemByName("Ankh") );


		level.mobs.add( shopkeeper );

		if (level instanceof LastShopLevel) {
			for (int i=0; i < Level.NEIGHBOURS9.length; i++) {
				int p = shopkeeper.getPos() + Level.NEIGHBOURS9[i];
				if (level.map[p] == Terrain.EMPTY_SP) {
					level.map[p] = Terrain.WATER;
				}
			}
		}
	}

	// the deleted Shopkeeper.collect applied a treasury check on everything it
	// stocked; the data-defined mobs' plain Char.collect does not, so the
	// painter does it here
	private static void stock( Level level, Mob shopkeeper, Item item ) {
		shopkeeper.collect( Treasury.getLevelTreasury().check( item ) );
	}

	private static Item kmd1(String kind) {
		return com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(kind).identify();
	}

	private static Item kmd(String a, String b) {
		return kmd1(Random.Int(2) == 0 ? a : b);
	}
}