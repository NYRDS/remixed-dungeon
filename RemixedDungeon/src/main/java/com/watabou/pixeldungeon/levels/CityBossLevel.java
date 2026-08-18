
package com.watabou.pixeldungeon.levels;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.levels.LevelTools;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.levels.objects.Sign;
import com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Heap;
import com.nyrds.pixeldungeon.items.Treasury;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
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
	private static final int HALL_HEIGHT	= 16;	// even so the 10-tall board ring centers (3-row margins)
	private static final int CHAMBER_HEIGHT	= 3;
	private static final int ANTE_HEIGHT	= 5;	// caveman: servant antechamber (taller for alcoves + decoration)
	private static final int CORRIDOR_HEIGHT = 5;	// caveman: longer entry corridor

	// caveman: a8 cell of the carved chess board (set when hero enters unarmed), or INVALID_CELL.
	private int chessBoardOrigin = INVALID_CELL;

	// caveman: antechamber bounds (for spawning servants in createMobs).
	private int anteLeft, anteTop, anteWidth, anteHeight;

	// caveman: anchor cells of the two loot rooms (INVALID_CELL = room not carved).
	private final int[] lootRoomCells = { INVALID_CELL, INVALID_CELL };


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

		int axisCol = _Left() + HALL_WIDTH / 2;  // consistent axis for exit, door, sign

		// caveman: throne room (hall) — chess board / King boss area.
		Painter.fill( this, _Left(), TOP, HALL_WIDTH, HALL_HEIGHT, Terrain.EMPTY );

		// caveman: edge statue columns frame the board symmetrically.
		int colL = _Left();
		int colR = _Left() + HALL_WIDTH - 1;
		for (int y = TOP + 1; y < TOP + HALL_HEIGHT; y += 2) {
			putLevelObject(LevelObjectsFactory.STATUE, y * getWidth() + colL);
			putLevelObject(LevelObjectsFactory.STATUE, y * getWidth() + colR);
		}

		// caveman: pedestals at mid-height on the edge columns.
		int midY = TOP + HALL_HEIGHT / 2;
		putLevelObject(LevelObjectsFactory.PEDESTAL, midY * getWidth() + colL);
		putLevelObject(LevelObjectsFactory.PEDESTAL, midY * getWidth() + colR);

		// caveman: 2 more pedestals on the axis, top+bottom of throne room — outside
		// the chess-carve 10x10 ring so they survive, more summon rotation points.
		int boardSize = 10;
		int carveTop = TOP + (HALL_HEIGHT - boardSize) / 2;
		putLevelObject(LevelObjectsFactory.PEDESTAL, cell(axisCol, carveTop - 1));
		putLevelObject(LevelObjectsFactory.PEDESTAL, cell(axisCol, carveTop + boardSize));

		// caveman: loot rooms flanking the throne — 5 wide x 6 tall, flush against the
		// hall's edge columns so a punched DOOR actually connects room to hall.
		int lootRoomW = 5, lootRoomH = 6;
		int lootTop = TOP + 3;
		int wLootL = _Left() - lootRoomW;       // west room: cols _Left()-5 .. _Left()-1 (col _Left()-1 = hall's west wall col)
		int eLootL = _Left() + HALL_WIDTH;      // east room: cols hall-right .. +5
		if (wLootL >= 1) {
			Painter.fill(this, wLootL, lootTop, lootRoomW, lootRoomH, Terrain.EMPTY);
			Painter.fill(this, wLootL, lootTop, lootRoomW, 1, Terrain.WALL);
			Painter.fill(this, wLootL, lootTop + lootRoomH - 1, lootRoomW, 1, Terrain.WALL);
			Painter.fill(this, wLootL, lootTop, 1, lootRoomH, Terrain.WALL);
			// caveman: punch the door through the room's right column (which is the
			// column adjacent to the hall) — connects loot room <-> throne hall.
			set(wLootL + lootRoomW - 1, lootTop + lootRoomH / 2, Terrain.DOOR);
			lootRoomCells[0] = cell(wLootL + 1, lootTop + 1);
		}
		if (eLootL + lootRoomW <= getWidth() - 1) {
			Painter.fill(this, eLootL, lootTop, lootRoomW, lootRoomH, Terrain.EMPTY);
			Painter.fill(this, eLootL, lootTop, lootRoomW, 1, Terrain.WALL);
			Painter.fill(this, eLootL, lootTop + lootRoomH - 1, lootRoomW, 1, Terrain.WALL);
			Painter.fill(this, eLootL + lootRoomW - 1, lootTop, 1, lootRoomH, Terrain.WALL);
			set(eLootL, lootTop + lootRoomH / 2, Terrain.DOOR); // door through room's left column (adjacent to hall)
			lootRoomCells[1] = cell(eLootL + 1, lootTop + 1);
		}

		// caveman: exit above the hall, on the axis column.
		setExit((TOP - 1) * getWidth() + axisCol, 0);
		map[getExit(0)] = Terrain.LOCKED_EXIT;

		// caveman: servant antechamber — hero MUST pass through to reach throne room.
		// Layout: two 5-cell-wide alcoves flanking a 3-wide central passage. Walls with
		// doorways separate alcoves from passage. Servants + decorations in the alcoves.
		int anteWallRow = TOP + HALL_HEIGHT;       // wall between throne and antechamber
		int anteInt = anteWallRow + 1;              // antechamber interior start
		int corridorWallRow = anteInt + ANTE_HEIGHT; // wall between antechamber and corridor
		int corridorLeft = axisCol - 1;             // left edge of central passage
		int corridorRight = axisCol + 1;            // right edge of central passage

		// caveman: wall between throne room and antechamber, 3-wide opening on axis.
		Painter.fill(this, _Left(), anteWallRow, HALL_WIDTH, 1, Terrain.WALL);
		set(axisCol - 1, anteWallRow, Terrain.EMPTY);
		arenaDoor = anteWallRow * getWidth() + axisCol;
		map[arenaDoor] = Terrain.DOOR;
		set(axisCol + 1, anteWallRow, Terrain.EMPTY);

		// caveman: antechamber interior — full hall width × ANTE_HEIGHT.
		Painter.fill(this, _Left(), anteInt, HALL_WIDTH, ANTE_HEIGHT, Terrain.EMPTY);

		// caveman: wall dividers between alcoves and central passage, with doorways.
		// Central passage = 4 wide (cols axisCol-2..axisCol+1), alcoves 4 wide each side.
		int alcoveWallL = axisCol - 3;   // col 13 — left alcove is cols _Left()..12 (3 wide)
		int alcoveWallR = axisCol + 2;   // col 18 — right alcove is cols 19.._Left()+HW-1 (3 wide)
		Painter.fill(this, alcoveWallL, anteInt, 1, ANTE_HEIGHT, Terrain.WALL);
		Painter.fill(this, alcoveWallR, anteInt, 1, ANTE_HEIGHT, Terrain.WALL);
		// doorways at mid-height of antechamber.
		int doorRow = anteInt + ANTE_HEIGHT / 2;
		set(alcoveWallL, doorRow, Terrain.DOOR);
		set(alcoveWallR, doorRow, Terrain.DOOR);

		// caveman: decorate the alcoves — statues in corners, pedestal in center.
		// Left alcove: cols _Left()..alcoveWallL-1 (= 10..14). Right: alcoveWallR+1.._Left()+HW-1 (=19..21).
		int leftAlcL = _Left(), leftAlcR = alcoveWallL - 1;
		int rightAlcL = alcoveWallR + 1, rightAlcR = _Left() + HALL_WIDTH - 1;
		int alcTop = anteInt, alcBot = anteInt + ANTE_HEIGHT - 1;
		// statues in alcove corners.
		putLevelObject(LevelObjectsFactory.STATUE, cell(leftAlcL + 1, alcTop + 1));
		putLevelObject(LevelObjectsFactory.STATUE, cell(rightAlcR - 1, alcBot - 1));
		// pedestals in alcove centers.
		putLevelObject(LevelObjectsFactory.PEDESTAL, cell((leftAlcL + leftAlcR) / 2, (alcTop + alcBot) / 2));
		putLevelObject(LevelObjectsFactory.PEDESTAL, cell((rightAlcL + rightAlcR) / 2, (alcTop + alcBot) / 2));

		// caveman: store alcove bounds for servant spawning (spawn in alcoves, not passage).
		anteLeft = leftAlcL; anteTop = anteInt; anteWidth = leftAlcR - leftAlcL + 1; anteHeight = ANTE_HEIGHT;

		// caveman: wall between antechamber and corridor, 3-wide opening on axis.
		Painter.fill(this, _Left(), corridorWallRow, HALL_WIDTH, 1, Terrain.WALL);
		set(axisCol - 1, corridorWallRow, Terrain.EMPTY);
		set(axisCol, corridorWallRow, Terrain.DOOR);
		set(axisCol + 1, corridorWallRow, Terrain.EMPTY);

		// caveman: narrow 3-wide entry corridor (longer).
		Painter.fill(this, corridorLeft, corridorWallRow + 1, 3, CORRIDOR_HEIGHT, Terrain.EMPTY);
		Painter.fill(this, corridorLeft, corridorWallRow + 1, 1, CORRIDOR_HEIGHT, Terrain.BOOKSHELF);
		Painter.fill(this, corridorLeft + 2, corridorWallRow + 1, 1, CORRIDOR_HEIGHT, Terrain.BOOKSHELF);

		entrance = (corridorWallRow + 1 + CORRIDOR_HEIGHT) * getWidth() + axisCol;
		map[entrance] = Terrain.ENTRANCE;

		// caveman: PalaceServants ScriptedActor manages faction behavior + phrases.
		ScriptedActor servantActor = new ScriptedActor("scripts/actors/PalaceServants");
		addScriptedActor(servantActor);

		return true;
	}

	@Override
	protected void createMobs() {
		// caveman: spawn neutral servants in BOTH alcoves.
		String[] servantTypes = {"Monk", "Warlock", "Golem"};
		int axisCol = _Left() + HALL_WIDTH / 2;
		int alcoveWallL = axisCol - 3;
		int alcoveWallR = axisCol + 2;
		// left alcove: _Left()..alcoveWallL-1, right alcove: alcoveWallR+1.._Left()+HALL_WIDTH-1
		int[][] alcoves = {
			{_Left(), alcoveWallL - 1},
			{alcoveWallR + 1, _Left() + HALL_WIDTH - 1}
		};
		for (int[] alcove : alcoves) {
			int count = 1 + Random.Int(2); // 1-2 per alcove
			for (int i = 0; i < count; i++) {
				int x = alcove[0] + Random.Int(alcove[1] - alcove[0] + 1);
				int y = anteTop + Random.Int(anteHeight);
				int c = cell(x, y);
				if (!cellValid(c) || !passable[c]) continue;
				var mob = MobFactory.mobByName(servantTypes[Random.Int(servantTypes.length)]);
				mob.setFraction(com.watabou.pixeldungeon.actors.mobs.Fraction.NEUTRAL);
				mob.setState(com.nyrds.pixeldungeon.ai.MobAi.getStateByClass(
					com.nyrds.pixeldungeon.ai.Passive.class));
				mob.setPos(c);
				// caveman: servants carry useful items — dropped when they die
				// (palace staff hold potions/scrolls/gear of the court).
				Treasury.Category[] carryCats = {
					Treasury.Category.POTION, Treasury.Category.SCROLL,
					Treasury.Category.FOOD, Treasury.Category.RING,
					Treasury.Category.WEAPON, Treasury.Category.ARMOR
				};
				var carried = Treasury.getLevelTreasury().random(
					carryCats[Random.Int(carryCats.length)]);
				if (carried != null) {
					mob.collect(carried);
				}
				mobs.add(mob);
				Actor.addDelayed(mob, 0);
			}
		}
	}

	@Override
	protected void createItems() {
		super.createItems(); // dropBones()

		// caveman: loot rooms — 2-3 chests each, ~80% cursed gear (curse unknown
		// until identified — the treasure is a trap for the greedy).
		int[] lootCats = {
			Treasury.Category.WEAPON.ordinal(),
			Treasury.Category.ARMOR.ordinal(),
			Treasury.Category.RING.ordinal(),
			Treasury.Category.WAND.ordinal()
		};
		for (int roomCell : lootRoomCells) {
			if (roomCell == INVALID_CELL || !cellValid(roomCell)) continue;
			int chests = 2 + Random.Int(2);
			for (int i = 0; i < chests; i++) {
				Treasury.Category cat = Treasury.Category.values()[
					lootCats[Random.Int(lootCats.length)]];
				var item = Treasury.getLevelTreasury().random(cat);
				if (item != null && Random.Float() < 0.8f) {
					item.setCursed(true);
					item.setCursedKnown(false);
				}
				int c = roomCell + Random.Int(3) + Random.Int(3) * getWidth();
				if (cellValid(c) && passable[c]) {
					drop(item, c, Heap.Type.CHEST);
				}
			}
		}
	}

	@Override
	protected void decorate() {

		LevelTools.northWallDecorate(this, 10, 8);

		int sign = arenaDoor + getWidth();  // caveman: below throne door, in antechamber
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
	public void updateFieldOfView(Char c) {
		super.updateFieldOfView(c);
		// caveman: during chess, force ALL cells visible. observeImpl copies fieldOfView -> visible,
		// so we set fieldOfView here (not visible — that gets overwritten by the arraycopy).
		if (chessBoardOrigin != INVALID_CELL) {
			for (int i = 0; i < getLength(); i++) {
				fieldOfView[i] = true;
			}
		}
	}

	@Override
	public String getProperty(String key, String defVal) {
		// caveman: expose carved chess board origin to Chess.lua. "" = no board (normal boss fight).
		if ("chessBoardOrigin".equals(key) && chessBoardOrigin != INVALID_CELL) {
			return Integer.toString(chessBoardOrigin);
		}
		// caveman: expose whether hero is armed for PalaceServants.lua.
		if ("heroIsArmed".equals(key)) {
			return heroUnarmed(Dungeon.hero) ? "false" : "true";
		}
		return super.getProperty(key, defVal);
	}

	// caveman: carve the 8x8 chess board + chasm ring into the hall. sets chessBoardOrigin (a8 cell).
	private void carveChessBoard() {
		int size = 10;            // 8 board + 2 chasm ring
		int left = _Left() + (HALL_WIDTH - size) / 2;   // horizontally centered (1-cell margin)
		int top  = TOP + (HALL_HEIGHT - size) / 2;      // vertically centered (3-row margin)

		for (int dy = 0; dy < size; dy++) {
			for (int dx = 0; dx < size; dx++) {
				int c = cell(left + dx, top + dy);
				// caveman: clear statues/pedestals from board area. they block LOS (fog),
				// block piece movement (AI moves fail), and clutter the board.
				LevelObject obj;
				while ((obj = getTopLevelObject(c)) != null) {
					obj.remove();  // caveman: kill sprite + remove logically. was Level.remove (no sprite kill).
				}
				boolean border = dx == 0 || dx == size - 1 || dy == 0 || dy == size - 1;
				if (border) {
					set(c, Terrain.CHASM);
				} else {
					set(c, ((dx + dy) % 2 == 0) ? Terrain.EMPTY_SP : Terrain.EMPTY);
				}
			}
		}

		chessBoardOrigin = cell(left + 1, top + 1);  // a8 = interior top-left

		// caveman: reveal entire level during chess (no fog of war). set mapped + visible for ALL cells.
		// don't call observe() — it would overwrite visible[] from LOS (hero can't see whole board).
		for (int i = 0; i < getLength(); i++) {
			mapped[i] = true;
			Dungeon.visible[i] = true;
		}

		// caveman: move hero just below the board so camera centers on it (hero is frozen for chess).
		// With HALL_HEIGHT=16 the board bottom ring ends 3 rows before the wall; place the hero there.
		int heroCell = cell(left + size / 2, top + size + 1);
		if (cellValid(heroCell) && passable[heroCell]) {
			WandOfBlink.appear(Dungeon.hero, heroCell);
		}

		GameScene.updateMap();
		Dungeon.observe();  // caveman: trigger fog update with forced-all-visible fieldOfView.
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

	// caveman: stalemate tie. King yields (NOT slain -> no boss-slain badge).
	// same board clear + exit unlock as onChessWin, minus the badge.
	@LuaInterface
	public void onChessTie() {
		if (chessBoardOrigin != INVALID_CELL) {
			int bx = cellX(chessBoardOrigin) - 1;
			int by = cellY(chessBoardOrigin) - 1;
			for (int dy = 0; dy < 10; dy++) {
				for (int dx = 0; dx < 10; dx++) {
					set(cell(bx + dx, by + dy), Terrain.EMPTY);
				}
			}
		}

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

			// caveman: boss must spawn in the throne hall itself (cols within hall
			// bounds) — NOT in the sealed loot rooms, where he'd be stuck behind
			// a door and the fight would look like it never started.
			int hallL = _Left() + 1, hallR = _Left() + HALL_WIDTH - 2; // interior cols
			do {
				pos = Random.Int(getLength());
			} while (
					!passable[pos] ||
							!outsideEntranceRoom(pos) ||
							Dungeon.isCellVisible(pos) ||
							cellX(pos) < hallL || cellX(pos) > hallR ||
							getTopLevelObject(pos) != null); // no spawning on pedestals/statues

			spawnBoss(pos);
			callServantsToKing();
		}
	}

	// caveman: the King's call — palace servants abandon their posts and join the
	// fight, pouring from the antechamber into the throne room.
	private void callServantsToKing() {
		for (Mob mob : getCopyOfMobsArray()) {
			String kind = mob.getEntityKind();
			if (kind.equals("Monk") || kind.equals("Warlock") || kind.equals("Golem")) {
				if (mob.friendly(Dungeon.hero)) {
					mob.setFraction(com.watabou.pixeldungeon.actors.mobs.Fraction.DUNGEON);
					mob.setState(com.nyrds.pixeldungeon.ai.MobAi.getStateByClass(
						com.nyrds.pixeldungeon.ai.Hunting.class));
					mob.beckon(Dungeon.hero.getPos());
				}
			}
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
