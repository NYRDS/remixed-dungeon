package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.items.books.TomeOfKnowledge;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.Torch;
import com.watabou.pixeldungeon.items.armor.LeatherArmor;
import com.watabou.pixeldungeon.items.bags.Keyring;
import com.watabou.pixeldungeon.items.bags.PotionBelt;
import com.watabou.pixeldungeon.items.bags.Quiver;
import com.watabou.pixeldungeon.items.bags.ScrollHolder;
import com.watabou.pixeldungeon.items.bags.SeedPouch;
import com.watabou.pixeldungeon.items.bags.WandHolster;
import com.watabou.pixeldungeon.items.food.OverpricedRation;
import com.watabou.pixeldungeon.items.weapon.melee.Dagger;
import com.watabou.pixeldungeon.items.weapon.melee.Knuckles;
import com.watabou.pixeldungeon.items.weapon.melee.Quarterstaff;
import com.watabou.pixeldungeon.items.weapon.melee.Sword;
import com.watabou.pixeldungeon.items.weapon.missiles.CommonArrow;
import com.watabou.pixeldungeon.items.weapon.missiles.Dart;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.painters.Painter;
import java.util.Arrays;

public class TownShopLevel extends Level {

	private static final int SIZE = 12;

	{
		color1 = 0x801500;
		color2 = 0xa68521;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_GENERIC_TOWN_INTERIOR;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_SEWERS;
	}

	@Override
	protected boolean build() {
		Arrays.fill( map, Terrain.WALL );
		Painter.fill( this, 2, 2, SIZE-2, SIZE-2, Terrain.EMPTY_SP );
		Painter.fill( this, SIZE/2, SIZE/2, 2, 2, Terrain.EMPTY );

		entrance = SIZE * getWidth() + SIZE / 2 + 1;
		map[entrance] = Terrain.ENTRANCE;


		setFeeling(Feeling.NONE);

		return true;
	}


	@Override
	protected void decorate() {
	}


	static public void fillInventory(Mob shopkeeper)
	{
		stock( shopkeeper, new LeatherArmor().identify() );
		stock( shopkeeper, new Dagger().identify() );
		stock( shopkeeper, new Knuckles().identify() );
		stock( shopkeeper, new Sword().identify() );
		stock( shopkeeper, new Quarterstaff().identify() );
		stock( shopkeeper, new TomeOfKnowledge().identify() );

		for (int i = 0; i <3; i++){
			stock( shopkeeper, new OverpricedRation() );
			stock( shopkeeper, new Dart(5).identify() );
			stock( shopkeeper, new CommonArrow(25) );
			stock( shopkeeper, new Torch().identify() );
		}

		stock( shopkeeper, new Keyring());
		stock( shopkeeper, new ScrollHolder());
		stock( shopkeeper, new PotionBelt());
		stock( shopkeeper, new SeedPouch());
		stock( shopkeeper, new Quiver());
		stock( shopkeeper, new WandHolster());
	}

	// the deleted Shopkeeper.collect applied a treasury check on everything it
	// stocked; the data-defined TownShopkeeper's plain Char.collect does not
	static private void stock(Mob shopkeeper, Item item)
	{
		shopkeeper.collect( Treasury.getLevelTreasury().check( item ) );
	}

	@Override
	protected void createMobs() {
		Mob shopkeeper = MobFactory.mobByName(MobFactory.TOWN_SHOPKEEPER);

		fillInventory(shopkeeper);

		shopkeeper.setPos(this.getRandomTerrainCell(Terrain.EMPTY));
		this.mobs.add( shopkeeper );
	}

	@Override
	protected void createItems() {
	}

	@Override
	public int randomRespawnCell() {
		return INVALID_CELL;
	}
}
