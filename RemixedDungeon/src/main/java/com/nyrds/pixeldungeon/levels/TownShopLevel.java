package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.items.common.ItemFactory;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.utils.Random;
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
		stock( shopkeeper, kmd1("Dagger") );
		stock( shopkeeper, kmd1("Knuckles") );
		stock( shopkeeper, kmd1("Sword") );
		stock( shopkeeper, kmd1("Quarterstaff") );

		for (int i = 0; i <3; i++){
			stock( shopkeeper, ItemFactory.itemByName(Food.OVERPRICED_RATION) );
			Item dart = ItemFactory.itemByName("Dart");
			dart.quantity(5);
			stock( shopkeeper, dart.identify() );
			Item arrows = ItemFactory.itemByName("CommonArrow");
			arrows.quantity(25);
			stock( shopkeeper, arrows.identify() );
			stock( shopkeeper, ItemFactory.itemByName("Torch").identify() );
		}

		stock( shopkeeper, ItemFactory.itemByName("Keyring"));
		stock( shopkeeper, ItemFactory.itemByName("ScrollHolder"));
		stock( shopkeeper, ItemFactory.itemByName("PotionBelt"));
		stock( shopkeeper, ItemFactory.itemByName("SeedPouch"));
		stock( shopkeeper, ItemFactory.itemByName("Quiver"));
		stock( shopkeeper, ItemFactory.itemByName("WandHolster"));
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

	private static Item kmd1(String kind) {
		return com.nyrds.pixeldungeon.items.common.ItemFactory.itemByName(kind).identify();
	}

	private static Item kmd(String a, String b) {
		return kmd1(Random.Int(2) == 0 ? a : b);
	}
}