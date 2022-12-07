package com.nyrds.retrodungeon.levels;

import com.nyrds.retrodungeon.items.necropolis.BlackSkull;
import com.nyrds.retrodungeon.items.necropolis.BlackSkullOfMastery;
import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.mobs.necropolis.Lich;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Bones;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.CityLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class NecroBossLevel extends Level {
	
	{
		color1 = 0x4b6636;
		color2 = 0xf2f2f2;
	}
	
	private static final int TOP			= 2;
	private static final int HALL_WIDTH		= 9;
	private static final int HALL_HEIGHT	= 9;
	private static final int CHAMBER_HEIGHT	= 4;
	
	private int arenaDoor;
	private boolean enteredArena = false;
	private boolean keyDropped = false;
	
	@Override
	public String tilesTex() {
		return Assets.TILES_NECRO;
	}
	
	@Override
	public String waterTex() {
		return Assets.WATER_NECRO;
	}
	
	private static final String DOOR	= "door";
	private static final String ENTERED	= "entered";
	private static final String DROPPED	= "droppped";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( DOOR, arenaDoor );
		bundle.put( ENTERED, enteredArena );
		bundle.put( DROPPED, keyDropped );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		arenaDoor = bundle.getInt( DOOR );
		enteredArena = bundle.getBoolean( ENTERED );
		keyDropped = bundle.getBoolean( DROPPED );
	}
	
	@Override
	protected boolean build() {

		Painter.fill( this, _Left(), TOP, HALL_WIDTH, HALL_HEIGHT, Terrain.WATER );
		
		int y = TOP + 1;
		while (y < TOP + HALL_HEIGHT) {
			map[y * getWidth() + _Center() - 3] = Terrain.STATUE;
			map[y * getWidth() + _Center() + 3] = Terrain.STATUE;
			y += 2;
		}

		int pedestal_1 = (TOP + HALL_HEIGHT / 4) * getWidth() + _Center() - 2;
		int pedestal_2 = (TOP + HALL_HEIGHT / 2 + HALL_HEIGHT / 4) * getWidth() + _Center() - 2;
		int pedestal_3 = (TOP + HALL_HEIGHT / 4) * getWidth() + _Center() + 2;
		int pedestal_4 = (TOP + HALL_HEIGHT / 2 + HALL_HEIGHT / 4) * getWidth() + _Center() + 2;

		map[pedestal_1] = map[pedestal_2] = map[pedestal_3] = map[pedestal_4] = Terrain.PEDESTAL;
		
		setExit((TOP - 1) * getWidth() + _Center(),0);
		
		arenaDoor = (TOP + HALL_HEIGHT) * getWidth() + _Center();
		map[arenaDoor] = Terrain.DOOR;
		
		Painter.fill( this, _Left(), TOP + HALL_HEIGHT + 1, HALL_WIDTH, CHAMBER_HEIGHT, Terrain.WATER );
		Painter.fill( this, _Left(), TOP + HALL_HEIGHT + 1, 1, CHAMBER_HEIGHT, Terrain.BOOKSHELF );
		Painter.fill( this, _Left() + HALL_WIDTH - 1, TOP + HALL_HEIGHT + 1, 1, CHAMBER_HEIGHT, Terrain.BOOKSHELF );
		
		entrance = (TOP + HALL_HEIGHT + 2 + Random.Int( CHAMBER_HEIGHT - 1 )) * getWidth() + _Left() + (/*1 +*/ Random.Int( HALL_WIDTH-2 )); 
		map[entrance] = Terrain.ENTRANCE;
		map[getExit(0)] = Terrain.LOCKED_EXIT;
		return true;
	}
	
	@Override
	protected void decorate() {

		for (int i=0; i < getLength(); i++) {
			if (map[i] == Terrain.WALL && Random.Int( 8 ) == 0) {
				map[i] = Terrain.WALL_DECO;
			}
		}
	}
	
	@Override
	protected void createMobs() {}
	
	@Override
	protected void createItems() {
		Item item = Bones.get();
		if (item != null) {
			int pos;
			do {
				pos = 
					Random.IntRange( _Left() + 1, _Left() + HALL_WIDTH - 2 ) + 
					Random.IntRange( TOP + HALL_HEIGHT + 1, TOP + HALL_HEIGHT  + CHAMBER_HEIGHT ) * getWidth();
			} while (pos == entrance);
			drop( item, pos ).type = Heap.Type.SKELETON;
		}
	}
	
	@Override
	public boolean isBossLevel() {
		return true;
	}
	
	@Override
	protected void pressHero(int cell, Hero hero) {

		super.pressHero( cell, hero );

		if (!enteredArena && outsideEntraceRoom( cell ) && hero == Dungeon.hero) {
			
			enteredArena = true;
			
			Lich boss = new Lich();
			boss.setState(boss.HUNTING);
			boss.setPos((TOP + HALL_HEIGHT / 2) * getWidth() + _Center());

			Dungeon.level.spawnMob(boss);

			set( arenaDoor, Terrain.LOCKED_DOOR );
			GameScene.updateMap( arenaDoor );
			Dungeon.observe();
		}
	}

	@Override
	public Heap drop( Item item, int cell ) {
		
		if (!keyDropped && (item instanceof BlackSkull || item instanceof BlackSkullOfMastery)) {
			
			keyDropped = true;
			
			set( arenaDoor, Terrain.DOOR );
			GameScene.updateMap( arenaDoor );
			Dungeon.observe();
		}
		
		return super.drop( item, cell );
	}
	
	private boolean outsideEntraceRoom( int cell ) {
		return cell / getWidth() < arenaDoor / getWidth();
	}
	
	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
			return Game.getVar(R.string.Prison_TileWater);
		case Terrain.HIGH_GRASS:
			return Game.getVar(R.string.City_TileHighGrass);
		case Terrain.UNLOCKED_EXIT:
		case Terrain.LOCKED_EXIT:
			return Game.getVar(R.string.PortalGate_Name);
		default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.BOOKSHELF:
			return Game.getVar(R.string.Halls_TileDescBookshelf);
		case Terrain.UNLOCKED_EXIT:
			return Utils.format(Game.getVar(R.string.PortalExit_Desc), Game.getVar(R.string.PortalExit_Desc_Necropolis));
		default:
			return super.tileDesc( tile );
		}
	}
	
	@Override
	public void addVisuals( Scene scene ) {
		CityLevel.addVisuals( this, scene );
	}

	private int _Left() {
		return (getWidth() - HALL_WIDTH) / 2;
	}

	private int _Center() {
		return _Left() + HALL_WIDTH / 2;
	}
}
