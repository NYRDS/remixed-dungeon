
package com.watabou.pixeldungeon.levels;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.guts.YogsEye;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

public class HallsBossLevel extends BossLevel {
	
	{
		color1 = 0x801500;
		color2 = 0xa68521;
		
		viewDistance = 3;
		_objectsKind = 5;
	}

	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_GUTS_XYZ;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_GUTS;
	}
	
	@Override
	public String waterTex() {
		return Assets.WATER_GUTS;
	}

	@Override
	protected boolean build() {
		
		for (int i=0; i < 5; i++) {
			
			int top = Random.IntRange( 2, _RoomTop() - 1 );
			int bottom = Random.IntRange( _RoomBottom() + 1, 22 );
			Painter.fill( this, 2 + i * 4, top, 4, bottom - top + 1, Terrain.EMPTY );
			
			if (i == 2) {
				setExit((i * 4 + 3) + (top - 1) * getWidth(),0);
			}
			
			for (int j=0; j < 4; j++) {
				if (Random.Int( 2 ) == 0) {
					int y = Random.IntRange( top + 1, bottom - 1 );
					map[i*4+j + y*getWidth()] = Terrain.WALL_DECO;
				}
			}
		}
		
		map[getExit(0)] = Terrain.LOCKED_EXIT;
		
		Painter.fill( this, _RoomLeft() - 1, _RoomTop() - 1, 
			_RoomRight() - _RoomLeft() + 3, _RoomBottom() - _RoomTop() + 3, Terrain.WALL );
		Painter.fill( this, _RoomLeft(), _RoomTop(), 
			_RoomRight() - _RoomLeft() + 1, _RoomBottom() - _RoomTop() + 1, Terrain.EMPTY );
		
		entrance = Random.Int( _RoomLeft() + 1, _RoomRight() - 1 ) + 
			Random.Int( _RoomTop() + 1, _RoomBottom() - 1 ) * getWidth();
		map[entrance] = Terrain.ENTRANCE;
		
		boolean[] patch = Patch.generate(this, 0.45f, 6 );
		for (int i=0; i < getLength(); i++) {
			if (map[i] == Terrain.EMPTY && patch[i]) {
				map[i] = Terrain.WATER;
			}
		}
		
		return true;
	}
	
	@Override
	protected void decorate() {	
		
		for (int i=0; i < getLength(); i++) {
			if (map[i] == Terrain.EMPTY && Random.Int( 10 ) == 0) { 
				map[i] = Terrain.EMPTY_DECO;
			}
		}
	}

	@Override
	protected void pressHero(int cell, Hero hero) {
		
		super.pressHero( cell, hero );
		
		if (!enteredArena && hero == Dungeon.hero && cell != entrance) {
			
			enteredArena = true;
			
			for (int i=_RoomLeft()-1; i <= _RoomRight() + 1; i++) {
				doMagic( (_RoomTop() - 1) * getWidth() + i );
				doMagic( (_RoomBottom() + 1) * getWidth() + i );
			}
			for (int i=_RoomTop(); i < _RoomBottom() + 1; i++) {
				doMagic( i * getWidth() + _RoomLeft() - 1 );
				doMagic( i * getWidth() + _RoomRight() + 1 );
			}
			GameScene.updateMap();

			Dungeon.observe();
			
			YogsEye boss = new YogsEye();
			do {
				boss.setPos(Random.Int( getLength() ));
			} while (
				!passable[boss.getPos()] || !passable[boss.getPos() - getWidth()] ||
				Dungeon.isCellVisible(boss.getPos()));
			spawnMob(boss);
			boss.spawnOrgans();
		}
	}

	private void doMagic( int cell ) {
		set( cell, Terrain.EMPTY_SP );
		CellEmitter.get( cell ).start( FlameParticle.FACTORY, 0.1f, 3 );
	}

	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
            return StringsManager.getVar(R.string.Halls_TileWater);
            case Terrain.GRASS:
                return StringsManager.getVar(R.string.Halls_TileGrass);
            case Terrain.HIGH_GRASS:
                return StringsManager.getVar(R.string.Halls_TileHighGrass);
            case Terrain.STATUE:
		case Terrain.STATUE_SP:
            return StringsManager.getVar(R.string.Halls_TileStatue);
            default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.WATER:
            return StringsManager.getVar(R.string.Halls_TileDescWater);
            case Terrain.STATUE:
		case Terrain.STATUE_SP:
            return StringsManager.getVar(R.string.Halls_TileDescStatue);
            default:
			return super.tileDesc( tile );
		}
	}
	
	@Override
	public void addVisuals( Scene scene ) {
		HallsLevel.addVisuals( this, scene );
	}

	private int _RoomLeft() {
		return getWidth() / 2 - 1;
	}

	private int _RoomRight() {
		return getWidth() / 2 + 1;
	}

	private int _RoomTop() {
		return getHeight() / 2 - 1;
	}

	private int _RoomBottom() {
		return getHeight() / 2 + 1;
	}

}
