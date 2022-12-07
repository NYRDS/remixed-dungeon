package com.nyrds.retrodungeon.levels;

import com.nyrds.retrodungeon.items.books.TomeOfKnowledge;
import com.nyrds.retrodungeon.mobs.npc.TownShopkeeper;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Heap;
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

import java.util.ArrayList;
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

		makeShop();
		return true;
	}

	private void makeShop(){
		ArrayList<Item> items = new ArrayList<>();

		items.add( new LeatherArmor().identify() );
		items.add( new Dagger().identify() );
		items.add( new Knuckles().identify() );
		items.add( new Sword().identify() );
		items.add( new Quarterstaff().identify() );
		items.add( new TomeOfKnowledge().identify() );

		for (int i = 0; i <3; i++){
			items.add( new OverpricedRation() );
			items.add( new Dart(5).identify() );
			items.add( new CommonArrow(25) );
			items.add( new Torch().identify() );
		}

		items.add( new Keyring());
		items.add( new ScrollHolder());
		items.add( new PotionBelt());
		items.add( new SeedPouch());
		items.add( new Quiver());
		items.add( new WandHolster());

		Item[] range = items.toArray(new Item[items.size()]);

		for (Item item : range) {
			itemForSell(item);
		}
	}

	public void itemForSell(Item item) {
		int cell;
		int ctr = 0;
		do {
			ctr++;
			if(ctr>25) {
				return;
			}

			cell = getRandomTerrainCell(Terrain.EMPTY_SP);

		} while (getHeap(cell) != null);

		drop(item, cell).type = Heap.Type.FOR_SALE;
	}

	@Override
	protected void decorate() {
	}

	@Override
	protected void createMobs() {
		Mob shopkeeper =  new TownShopkeeper();
		shopkeeper.setPos(this.getRandomTerrainCell(Terrain.EMPTY));
		this.mobs.add( shopkeeper );
	}

	@Override
	protected void createItems() {
	}

	@Override
	public int randomRespawnCell() {
		return -1;
	}

	@Override
	public String tileName( int tile ) {
		switch (tile) {
		default:
			return super.tileName( tile );
		}
	}

	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		default:
			return super.tileDesc(tile);
		}
	}
}
