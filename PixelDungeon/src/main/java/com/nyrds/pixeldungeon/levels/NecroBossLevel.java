package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.necropolis.RunicSkull;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Bones;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.CityLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;

public class NecroBossLevel extends Level {
	
	{
		color1 = 0x4b6636;
		color2 = 0xf2f2f2;
	}
	
	private static final int TOP			= 2;
	private static final int HALL_WIDTH		= 9;
	private static final int HALL_HEIGHT	= 9;
	private static final int CHAMBER_HEIGHT	= 4;
	private static final int SKULLS_BY_DEFAULT	= 3;
	
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
			map[y * getWidth() + _Center() - 3] = Terrain.STATUE_SP;
			map[y * getWidth() + _Center() + 3] = Terrain.STATUE_SP;
			y += 2;
		}

		int pedestal_1 = (TOP + HALL_HEIGHT / 4) * getWidth() + _Center() - 2;
		int pedestal_2 = (TOP + HALL_HEIGHT / 2 + HALL_HEIGHT / 4) * getWidth() + _Center() - 2;
		int pedestal_3 = (TOP + HALL_HEIGHT / 4) * getWidth() + _Center() + 2;
		int pedestal_4 = (TOP + HALL_HEIGHT / 2 + HALL_HEIGHT / 4) * getWidth() + _Center() + 2;

		map[pedestal_1] = map[pedestal_2] = map[pedestal_3] = map[pedestal_4] = Terrain.PEDESTAL;
		
		setExit((TOP - 1) * getWidth() + _Center(),0);
		map[getExit(0)] = Terrain.LOCKED_EXIT;
		
		arenaDoor = (TOP + HALL_HEIGHT) * getWidth() + _Center();
		map[arenaDoor] = Terrain.DOOR;
		
		Painter.fill( this, _Left(), TOP + HALL_HEIGHT + 1, HALL_WIDTH, CHAMBER_HEIGHT, Terrain.WATER );
		Painter.fill( this, _Left(), TOP + HALL_HEIGHT + 1, 1, CHAMBER_HEIGHT, Terrain.BOOKSHELF );
		Painter.fill( this, _Left() + HALL_WIDTH - 1, TOP + HALL_HEIGHT + 1, 1, CHAMBER_HEIGHT, Terrain.BOOKSHELF );
		
		entrance = (TOP + HALL_HEIGHT + 2 + Random.Int( CHAMBER_HEIGHT - 1 )) * getWidth() + _Left() + (/*1 +*/ Random.Int( HALL_WIDTH-2 )); 
		map[entrance] = Terrain.ENTRANCE;

		return true;
	}
	
	@Override
	protected void decorate() {	
		
		for (int i=0; i < getLength(); i++) {
			if (map[i] == Terrain.WATER && Random.Int( 10 ) == 0) {
				map[i] = Terrain.EMPTY_DECO;
			} else if (map[i] == Terrain.WALL && Random.Int( 8 ) == 0) { 
				map[i] = Terrain.WALL_DECO;
			}
		}
		
		int sign = arenaDoor + getWidth() + 1;
		map[sign] = Terrain.SIGN;
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
			} while (pos == entrance || map[pos] == Terrain.SIGN);
			drop( item, pos ).type = Heap.Type.SKELETON;
		}
	}
	
	@Override
	public boolean isBossLevel() {
		return true;
	}
	
	@Override
	public void pressHero(int cell, Hero hero ) {
		
		super.pressHero( cell, hero );
		
		if (!enteredArena && outsideEntraceRoom( cell ) && hero == Dungeon.hero) {
			
			enteredArena = true;
			
			Mob boss = Bestiary.mob( Dungeon.depth, levelKind() );
			boss.state = boss.HUNTING;
			do {
				boss.setPos(Random.Int( getLength() ));
			} while (
				!passable[boss.getPos()] ||
				!outsideEntraceRoom( boss.getPos() ) ||
				Dungeon.visible[boss.getPos()]);
			Dungeon.level.spawnMob(boss);

			SpawnSkulls();

			set( arenaDoor, Terrain.LOCKED_DOOR );
			GameScene.updateMap( arenaDoor );
			Dungeon.observe();
		}
	}

	public void SpawnSkulls(){

		int nSkulls = SKULLS_BY_DEFAULT;
		if(Game.getDifficulty() < 2){
			nSkulls = 2;
		}
		else if(Game.getDifficulty() > 2){
			nSkulls = 4;
		}

		List<Integer> occupiedPedestals = new ArrayList<Integer>();
		int i = 0;
		while (i < nSkulls){
			int skullCell = Dungeon.level.getRandomTerrainCell(Terrain.PEDESTAL);
			if (Dungeon.level.cellValid(skullCell)) {
				if (!occupiedPedestals.contains(skullCell)) {
					Mob mob = new RunicSkull();
					Dungeon.level.spawnMob(mob);
					WandOfBlink.appear(mob, skullCell);
					occupiedPedestals.add(skullCell);
					i++;
				}
			}
		}

	}

	@Override
	public Heap drop( Item item, int cell ) {
		
		if (!keyDropped && item instanceof SkeletonKey) {
			
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
			return Game.getVar(R.string.City_TileWater);
		case Terrain.HIGH_GRASS:
			return Game.getVar(R.string.City_TileHighGrass);
		default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.ENTRANCE:
			return Game.getVar(R.string.City_TileDescEntrance);
		case Terrain.EXIT:
			return Game.getVar(R.string.City_TileDescExit);
		case Terrain.WALL_DECO:
		case Terrain.EMPTY_DECO:
			return Game.getVar(R.string.City_TileDescDeco);
		case Terrain.EMPTY_SP:
			return Game.getVar(R.string.City_TileDescEmptySP);
		case Terrain.STATUE:
		case Terrain.STATUE_SP:
			return Game.getVar(R.string.City_TileDescStatue);
		case Terrain.BOOKSHELF:
			return Game.getVar(R.string.City_TileDescBookshelf);
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
