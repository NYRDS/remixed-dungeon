
package com.watabou.pixeldungeon.levels.painters;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.items.books.TomeOfKnowledge;
import com.nyrds.pixeldungeon.items.guts.armor.GothicArmor;
import com.nyrds.pixeldungeon.items.guts.weapon.melee.Claymore;
import com.nyrds.pixeldungeon.items.guts.weapon.melee.Halberd;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Ankh;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.Torch;
import com.watabou.pixeldungeon.items.Weightstone;
import com.watabou.pixeldungeon.items.armor.LeatherArmor;
import com.watabou.pixeldungeon.items.armor.MailArmor;
import com.watabou.pixeldungeon.items.armor.PlateArmor;
import com.watabou.pixeldungeon.items.armor.ScaleArmor;
import com.watabou.pixeldungeon.items.food.OverpricedRation;
import com.watabou.pixeldungeon.items.potions.PotionOfExperience;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.pixeldungeon.items.potions.PotionOfMight;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfIdentify;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.watabou.pixeldungeon.items.weapon.melee.BattleAxe;
import com.watabou.pixeldungeon.items.weapon.melee.Glaive;
import com.watabou.pixeldungeon.items.weapon.melee.Longsword;
import com.watabou.pixeldungeon.items.weapon.melee.Mace;
import com.watabou.pixeldungeon.items.weapon.melee.Quarterstaff;
import com.watabou.pixeldungeon.items.weapon.melee.Spear;
import com.watabou.pixeldungeon.items.weapon.melee.Sword;
import com.watabou.pixeldungeon.items.weapon.melee.WarHammer;
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
				stock( level, shopkeeper, (Random.Int( 2 ) == 0 ? new Quarterstaff() : new Spear()).identify() );
				stock( level, shopkeeper, new LeatherArmor().identify() );
				stock( level, shopkeeper, new Weightstone() );
				stock( level, shopkeeper, new TomeOfKnowledge().identify() );
				break;

			case 11:
				stock( level, shopkeeper, (Random.Int( 2 ) == 0 ? new Sword() : new Mace()).identify() );
				stock( level, shopkeeper, new MailArmor().identify() );
				stock( level, shopkeeper, new Weightstone() );
				stock( level, shopkeeper, new TomeOfKnowledge().identify() );
				break;

			case 16:
				stock( level, shopkeeper, (Random.Int( 2 ) == 0 ? new Longsword() : new BattleAxe()).identify() );
				stock( level, shopkeeper, new ScaleArmor().identify() );
				stock( level, shopkeeper, new Weightstone() );
				stock( level, shopkeeper, new TomeOfKnowledge().identify() );
				break;

			case 21:
				switch (Random.Int( 3 )) {
					case 0:
						stock( level, shopkeeper, new Glaive().identify() );
						break;
					case 1:
						stock( level, shopkeeper, new WarHammer().identify() );
						break;
					case 2:
						stock( level, shopkeeper, new PlateArmor().identify() );
						break;
				}
				stock( level, shopkeeper, new Weightstone() );
				stock( level, shopkeeper, new Torch() );
				stock( level, shopkeeper, new Torch() );
				break;

			case 27:
				switch (Random.Int( 3 )) {
					case 0:
						stock( level, shopkeeper, new Claymore().identify() );
						break;
					case 1:
						stock( level, shopkeeper, new Halberd().identify() );
						break;
					case 2:
						stock( level, shopkeeper, new GothicArmor().identify() );
						break;
				}
				stock( level, shopkeeper, new PotionOfHealing() );
				stock( level, shopkeeper, new PotionOfExperience());
				stock( level, shopkeeper, new PotionOfMight());
				break;
		}

		stock( level, shopkeeper, new PotionOfHealing() );
		for (int i=0; i < 2; i++) {
			stock( level, shopkeeper, Treasury.getLevelTreasury().random( Treasury.Category.POTION ) );
		}

		stock( level, shopkeeper, new ScrollOfIdentify() );
		stock( level, shopkeeper, new ScrollOfRemoveCurse() );
		stock( level, shopkeeper, new ScrollOfMagicMapping() );
		stock( level, shopkeeper, Treasury.getLevelTreasury().random( Treasury.Category.SCROLL ) );

		stock( level, shopkeeper, new OverpricedRation() );
		stock( level, shopkeeper, new OverpricedRation() );

		stock( level, shopkeeper, new Ankh() );


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
}
