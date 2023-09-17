
package com.watabou.pixeldungeon.levels.painters;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.items.books.TomeOfKnowledge;
import com.nyrds.pixeldungeon.items.guts.armor.GothicArmor;
import com.nyrds.pixeldungeon.items.guts.weapon.melee.Claymore;
import com.nyrds.pixeldungeon.items.guts.weapon.melee.Halberd;
import com.nyrds.pixeldungeon.mobs.npc.AzuterronNPC;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.npcs.ImpShopkeeper;
import com.watabou.pixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.watabou.pixeldungeon.items.Ankh;
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


		Shopkeeper shopkeeper = level instanceof LastShopLevel ? new ImpShopkeeper() : new Shopkeeper();
		if (Dungeon.depth == 27) {
			shopkeeper = new AzuterronNPC();
		}
		shopkeeper.setPos(pos);

		switch (Dungeon.depth) {

			case 6:
				shopkeeper.collect( (Random.Int( 2 ) == 0 ? new Quarterstaff() : new Spear()).identify() );
				shopkeeper.collect( new LeatherArmor().identify() );
				shopkeeper.collect( new Weightstone() );
				shopkeeper.collect( new TomeOfKnowledge().identify() );
				break;

			case 11:
				shopkeeper.collect( (Random.Int( 2 ) == 0 ? new Sword() : new Mace()).identify() );
				shopkeeper.collect( new MailArmor().identify() );
				shopkeeper.collect( new Weightstone() );
				shopkeeper.collect( new TomeOfKnowledge().identify() );
				break;

			case 16:
				shopkeeper.collect( (Random.Int( 2 ) == 0 ? new Longsword() : new BattleAxe()).identify() );
				shopkeeper.collect( new ScaleArmor().identify() );
				shopkeeper.collect( new Weightstone() );
				shopkeeper.collect( new TomeOfKnowledge().identify() );
				break;

			case 21:
				switch (Random.Int( 3 )) {
					case 0:
						shopkeeper.collect( new Glaive().identify() );
						break;
					case 1:
						shopkeeper.collect( new WarHammer().identify() );
						break;
					case 2:
						shopkeeper.collect( new PlateArmor().identify() );
						break;
				}
				shopkeeper.collect( new Weightstone() );
				shopkeeper.collect( new Torch() );
				shopkeeper.collect( new Torch() );
				break;

			case 27:
				switch (Random.Int( 3 )) {
					case 0:
						shopkeeper.collect( new Claymore().identify() );
						break;
					case 1:
						shopkeeper.collect( new Halberd().identify() );
						break;
					case 2:
						shopkeeper.collect( new GothicArmor().identify() );
						break;
				}
				shopkeeper.collect( new PotionOfHealing() );
				shopkeeper.collect( new PotionOfExperience());
				shopkeeper.collect( new PotionOfMight());
				break;
		}

		shopkeeper.collect( new PotionOfHealing() );
		for (int i=0; i < 2; i++) {
			shopkeeper.collect( Treasury.getLevelTreasury().random( Treasury.Category.POTION ) );
		}

		shopkeeper.collect( new ScrollOfIdentify() );
		shopkeeper.collect( new ScrollOfRemoveCurse() );
		shopkeeper.collect( new ScrollOfMagicMapping() );
		shopkeeper.collect( Treasury.getLevelTreasury().random( Treasury.Category.SCROLL ) );

		shopkeeper.collect( new OverpricedRation() );
		shopkeeper.collect( new OverpricedRation() );

		shopkeeper.collect( new Ankh() );


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
}
