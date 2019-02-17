package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.items.books.TomeOfKnowledge;
import com.nyrds.pixeldungeon.mobs.npc.TownShopkeeper;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.mobs.npcs.Shopkeeper;
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


	static public void fillInventory(Shopkeeper shopkeeper)
	{
		shopkeeper.addItem( new LeatherArmor().identify() );
		shopkeeper.addItem( new Dagger().identify() );
		shopkeeper.addItem( new Knuckles().identify() );
		shopkeeper.addItem( new Sword().identify() );
		shopkeeper.addItem( new Quarterstaff().identify() );
		shopkeeper.addItem( new TomeOfKnowledge().identify() );

		for (int i = 0; i <3; i++){
			shopkeeper.addItem( new OverpricedRation() );
			shopkeeper.addItem( new Dart(5).identify() );
			shopkeeper.addItem( new CommonArrow(25) );
			shopkeeper.addItem( new Torch().identify() );
		}

		shopkeeper.addItem( new Keyring());
		shopkeeper.addItem( new ScrollHolder());
		shopkeeper.addItem( new PotionBelt());
		shopkeeper.addItem( new SeedPouch());
		shopkeeper.addItem( new Quiver());
		shopkeeper.addItem( new WandHolster());
	}

	@Override
	protected void createMobs() {
		Shopkeeper shopkeeper =  new TownShopkeeper();

		fillInventory(shopkeeper);

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
