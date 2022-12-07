package com.nyrds.retrodungeon.levels;

import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.mobs.icecaves.IceGuardian;
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
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class IceCavesBossLevel extends Level {
	
	{
		color1 = 0x4b6636;
		color2 = 0xf2f2f2;
	}
	
	private static final int TOP			= 2;
	private static final int HALL_WIDTH		= 11;
	private static final int HALL_HEIGHT	= 9;
	private static final int CHAMBER_HEIGHT	= 1;
	
	private int arenaDoor;
	private boolean enteredArena = false;
	private boolean keyDropped = false;
	
	@Override
	public String tilesTex() {
		return Assets.TILES_ICE_CAVES_X;
	}
	
	@Override
	public String waterTex() {
		return Assets.WATER_ICE_CAVES;
	}
	
	private static final String DOOR	= "door";
	private static final String ENTERED	= "entered";
	private static final String DROPPED	= "dropped";
	
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

		Painter.fill( this, _Left(), TOP, HALL_WIDTH, HALL_HEIGHT, Terrain.EMPTY_SP );
		Painter.fill( this, _Left(), TOP, HALL_WIDTH, TOP, Terrain.EMPTY );
		Painter.fill( this, _Left(), HALL_HEIGHT, HALL_WIDTH, TOP, Terrain.EMPTY );
		for (int i = 0; i < 10; i++) {
			map[getRandomTerrainCell(Terrain.EMPTY_SP)] = Terrain.STATUE_SP;
		}
		
		arenaDoor = (TOP + HALL_HEIGHT) * getWidth() + _Center();
		map[arenaDoor] = Terrain.DOOR;
		
		Painter.fill( this, _Left(), TOP + HALL_HEIGHT + 1, HALL_WIDTH, CHAMBER_HEIGHT, Terrain.EMPTY );
		
		entrance = (TOP + HALL_HEIGHT + 2 + Random.Int( CHAMBER_HEIGHT - 1 )) * getWidth() + _Left() + (/*1 +*/ Random.Int( HALL_WIDTH-2 )); 
		map[entrance] = Terrain.ENTRANCE;
		setExit((TOP - 1) * getWidth() + _Center(),0);
		map[getExit(0)] = Terrain.LOCKED_EXIT;
		return true;
	}
	
	@Override
	protected void decorate() {	
		
		for (int i=0; i < getLength(); i++) {
			if (map[i] == Terrain.EMPTY && Random.Int( 10 ) == 0) { 
				map[i] = Terrain.EMPTY_DECO;
			} else if (map[i] == Terrain.WALL && Random.Int( 8 ) == 0) { 
				map[i] = Terrain.WALL_DECO;
			}
		}
	}
	
	public int pedestal( boolean left ) {
		if (left) {
			return (TOP + HALL_HEIGHT / 2) * getWidth() + _Center() - 2;
		} else {
			return (TOP + HALL_HEIGHT / 2) * getWidth() + _Center() + 2;
		}
	}
	
	@Override
	protected void createMobs() {	
	}
	
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
	public void pressHero(int cell, Hero hero ) {
		
		super.pressHero( cell, hero );
		
		if (!enteredArena && outsideEntraceRoom( cell ) && hero == Dungeon.hero) {
			
			enteredArena = true;
			
			Mob boss = Bestiary.mob(this);
			Mob guard = new IceGuardian();

			Mob mob = boss;

			for (int i = 0; i < 2; i++){
				mob.setState(mob.HUNTING);
				do {
					mob.setPos(Random.Int( getLength() ));
				} while (
						!passable[mob.getPos()] ||
								!outsideEntraceRoom( mob.getPos() ) ||
								Dungeon.visible[mob.getPos()]);
				Dungeon.level.spawnMob(mob);
				mob = guard;
			}

			set( arenaDoor, Terrain.LOCKED_DOOR );
			GameScene.updateMap( arenaDoor );
			Dungeon.observe();
		}
	}

	@Override
	public void unseal() {
		set( arenaDoor, Terrain.DOOR );
		GameScene.updateMap( arenaDoor );
		Dungeon.observe();
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
			case Terrain.GRASS:
				return Game.getVar(R.string.IceCaves_TileGrass);
			case Terrain.HIGH_GRASS:
				return Game.getVar(R.string.IceCaves_TileHighGrass);
			case Terrain.WATER:
				return Game.getVar(R.string.Caves_TileWater);
			case Terrain.STATUE:
			case Terrain.STATUE_SP:
				return Game.getVar(R.string.IceCaves_TileStatue);
			case Terrain.UNLOCKED_EXIT:
			case Terrain.LOCKED_EXIT:
				return Game.getVar(R.string.PortalGate_Name);
			default:
				return super.tileName( tile );
		}
	}

	@Override
	public String tileDesc( int tile ) {
		switch (tile) {
			case Terrain.ENTRANCE:
				return Game.getVar(R.string.Caves_TileDescEntrance);
			case Terrain.EXIT:
				return Game.getVar(R.string.Caves_TileDescExit);
			case Terrain.HIGH_GRASS:
				return Game.getVar(R.string.IceCaves_TileDescHighGrass);
			case Terrain.WALL_DECO:
				return Game.getVar(R.string.IceCaves_TileDescDeco);
			case Terrain.BOOKSHELF:
				return Game.getVar(R.string.Caves_TileDescBookshelf);
			case Terrain.STATUE:
			case Terrain.STATUE_SP:
				return Game.getVar(R.string.IceCaves_TileDescStatue);
			case Terrain.UNLOCKED_EXIT:
				return Utils.format(Game.getVar(R.string.PortalExit_Desc), Game.getVar(R.string.PortalExit_Desc_IceCaves));
			default:
				return super.tileDesc( tile );
		}
	}

	@Override
	public void addVisuals( Scene scene ) {
		IceCavesLevel.addVisuals( this, scene );
	}

	private int _Left() {
		return (getWidth() - HALL_WIDTH) / 2;
	}

	private int _Center() {
		return _Left() + HALL_WIDTH / 2;
	}
}
