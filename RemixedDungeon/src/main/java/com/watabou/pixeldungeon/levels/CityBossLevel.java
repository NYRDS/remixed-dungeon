
package com.watabou.pixeldungeon.levels;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.levels.LevelTools;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.levels.objects.Sign;
import com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.painters.Painter;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class CityBossLevel extends BossLevel {
	
	{
		color1 = 0x4b6636;
		color2 = 0xf2f2f2;
		_objectsKind =3;
	}
	
	private static final int TOP			= 2;
	private static final int HALL_WIDTH		= 12;	// widened from 7 so the 8x8 chess board + ring fits
	private static final int HALL_HEIGHT	= 15;
	private static final int CHAMBER_HEIGHT	= 3;

	// caveman: a8 cell of the carved chess board (set when hero enters unarmed), or INVALID_CELL.
	private int chessBoardOrigin = INVALID_CELL;


	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_CITY_XYZ;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_CITY;
	}
	
	@Override
	public String waterTex() {
		return Assets.WATER_CITY;
	}

	@Override
	protected boolean build() {
		
		Painter.fill( this, _Left(), TOP, HALL_WIDTH, HALL_HEIGHT, Terrain.EMPTY );
		Painter.fill( this, _Center(), TOP, 1, HALL_HEIGHT, Terrain.EMPTY_SP );

		int left = (TOP + HALL_HEIGHT / 2) * getWidth() + _Center() - 2;
		int right = (TOP + HALL_HEIGHT / 2) * getWidth() + _Center() + 2;

		int y = TOP + 1;
		while (y < TOP + HALL_HEIGHT) {
			putLevelObject(LevelObjectsFactory.STATUE, y * getWidth() + _Center() - 2);
			putLevelObject(LevelObjectsFactory.STATUE, y * getWidth() + _Center() + 2);
			y += 2;
		}

		putLevelObject(LevelObjectsFactory.PEDESTAL, left);
		putLevelObject(LevelObjectsFactory.PEDESTAL, right);

		for (int i=left+1; i < right; i++) {
			map[i] = Terrain.EMPTY_SP;
		}


		setExit((TOP - 1) * getWidth() + _Center(),0);
		map[getExit(0)] = Terrain.LOCKED_EXIT;
		
		arenaDoor = (TOP + HALL_HEIGHT) * getWidth() + _Center();
		map[arenaDoor] = Terrain.DOOR;
		
		Painter.fill( this, _Left(), TOP + HALL_HEIGHT + 1, HALL_WIDTH, CHAMBER_HEIGHT, Terrain.EMPTY );
		Painter.fill( this, _Left(), TOP + HALL_HEIGHT + 1, 1, CHAMBER_HEIGHT, Terrain.BOOKSHELF );
		Painter.fill( this, _Left() + HALL_WIDTH - 1, TOP + HALL_HEIGHT + 1, 1, CHAMBER_HEIGHT, Terrain.BOOKSHELF );
		
		entrance = (TOP + HALL_HEIGHT + 2 + Random.Int( CHAMBER_HEIGHT - 1 )) * getWidth() + _Left() + (/*1 +*/ Random.Int( HALL_WIDTH-2 )); 
		map[entrance] = Terrain.ENTRANCE;
		
		return true;
	}
	
	@Override
	protected void decorate() {

		LevelTools.northWallDecorate(this, 10, 8);

		int sign = arenaDoor + getWidth() + 1;
		addLevelObject(new Sign(sign, StringsManager.getVar(R.string.CityBossLevel_ChessSign)));
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("chessBoardOrigin", chessBoardOrigin);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		chessBoardOrigin = bundle.optInt("chessBoardOrigin", INVALID_CELL);
	}

	@Override
	public String getProperty(String key, String defVal) {
		// caveman: expose carved chess board origin to Chess.lua. "" = no board (normal boss fight).
		if ("chessBoardOrigin".equals(key) && chessBoardOrigin != INVALID_CELL) {
			return Integer.toString(chessBoardOrigin);
		}
		return super.getProperty(key, defVal);
	}

	// caveman: carve the 8x8 chess board + chasm ring into the hall. sets chessBoardOrigin (a8 cell).
	private void carveChessBoard() {
		int left = _Left() + 1;   // ring left (HALL_WIDTH=12, board ring=10, 1-cell margin each side)
		int top = TOP + 3;        // ring top
		int size = 10;            // 8 board + 2 chasm ring

		for (int dy = 0; dy < size; dy++) {
			for (int dx = 0; dx < size; dx++) {
				int c = cell(left + dx, top + dy);
				boolean border = dx == 0 || dx == size - 1 || dy == 0 || dy == size - 1;
				if (border) {
					set(c, Terrain.CHASM);
				} else {
					set(c, ((dx + dy) % 2 == 0) ? Terrain.EMPTY_SP : Terrain.EMPTY);
				}
			}
		}

		chessBoardOrigin = cell(left + 1, top + 1);  // a8 = interior top-left
		GameScene.updateMap();
	}

	private boolean heroUnarmed(Hero hero) {
		Belongings b = hero.getBelongings();
		return b.getItemFromSlot(Belongings.Slot.WEAPON) == ItemsList.DUMMY
				&& b.getItemFromSlot(Belongings.Slot.LEFT_HAND) == ItemsList.DUMMY;
	}

	// caveman: player won chess. King defeated (badge), board cleared (chasm -> floor), exit unlocked.
	@LuaInterface
	public void onChessWin() {
		Badges.validateBossSlain(Badges.Badge.BOSS_SLAIN_4);

		// clear the carved board area (10x10 ring around origin) so hero can walk.
		if (chessBoardOrigin != INVALID_CELL) {
			int bx = cellX(chessBoardOrigin) - 1;
			int by = cellY(chessBoardOrigin) - 1;
			for (int dy = 0; dy < 10; dy++) {
				for (int dx = 0; dx < 10; dx++) {
					set(cell(bx + dx, by + dy), Terrain.EMPTY);
				}
			}
		}

		// unlock the exit (was LOCKED_EXIT since boss never spawned).
		int exit = getExit(0);
		if (cellValid(exit)) {
			set(exit, Terrain.UNLOCKED_EXIT);
		}

		GameScene.updateMap();
	}

	@Override
	public void pressHero(int cell, Hero hero ) {

		super.pressHero( cell, hero );

		if (!enteredArena && outsideEntranceRoom( cell ) && hero == Dungeon.hero) {

			enteredArena = true;

			// caveman: King chess challenge. hero unarmed (no weapon, no left hand) -> carve board,
			// run Chess script; skip the boss fight. armed -> normal King fight below.
			if (heroUnarmed(hero)) {
				carveChessBoard();
				ScriptedActor chessActor = new ScriptedActor("scripts/actors/Chess");
				addScriptedActor(chessActor);
				chessActor.activate();
				return;
			}

			int pos;

			do {
				pos = Random.Int(getLength());
			} while (
					!passable[pos] ||
							!outsideEntranceRoom(pos ) ||
							Dungeon.isCellVisible(pos));

			spawnBoss(pos);
		}
	}

	private boolean outsideEntranceRoom(int cell ) {
		return cell / getWidth() < arenaDoor / getWidth();
	}
	
	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
            return StringsManager.getVar(R.string.City_TileWater);
            case Terrain.HIGH_GRASS:
                return StringsManager.getVar(R.string.City_TileHighGrass);
            default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.ENTRANCE:
            return StringsManager.getVar(R.string.City_TileDescEntrance);
            case Terrain.EXIT:
                return StringsManager.getVar(R.string.City_TileDescExit);
            case Terrain.WALL_DECO:
		case Terrain.EMPTY_DECO:
            return StringsManager.getVar(R.string.City_TileDescDeco);
            case Terrain.EMPTY_SP:
                return StringsManager.getVar(R.string.City_TileDescEmptySP);
            case Terrain.STATUE:
		case Terrain.STATUE_SP:
            return StringsManager.getVar(R.string.City_TileDescStatue);
            case Terrain.BOOKSHELF:
                return StringsManager.getVar(R.string.City_TileDescBookshelf);
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
