/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.levels;

import com.nyrds.pixeldungeon.effects.emitters.WaterSink;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.ScarecrowNPC;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.items.DewVial;
import com.watabou.utils.Random;

public class SewerLevel extends RegularLevel {

	{
		color1 = 0x48763c;
		color2 = 0x59994a;
	}

	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_SEWERS_XYZ;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_SEWERS;
	}

	@Override
	public String tilesTexEx() {
		return Assets.TILES_SEWERS_X;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_SEWERS;
	}

	protected boolean[] water() {
		return Patch.generate(this, getFeeling() == Feeling.WATER ? 0.60f : 0.45f, 5);
	}

	protected boolean[] grass() {
		return Patch.generate(this, getFeeling() == Feeling.GRASS ? 0.60f : 0.40f, 4);
	}

	@Override
	protected void decorate() {

		final int width = getWidth();
		for (int i = 0; i < width; i++) {
			if (map[i] == Terrain.WALL &&
					map[i + width] == Terrain.WATER &&
					Random.Int(4) == 0) {

				map[i] = Terrain.WALL_DECO;
			}
		}

		final int length = getLength();

		for (int i = width; i < length - width; i++) {
			if (map[i] == Terrain.WALL &&
					map[i - width] == Terrain.WALL &&
					map[i + width] == Terrain.WATER &&
					Random.Int(2) == 0) {

				map[i] = Terrain.WALL_DECO;
			}
		}

		for (int i = width + 1; i < length - width - 1; i++) {
			if (map[i] == Terrain.EMPTY) {

				int count =
						(map[i + 1] == Terrain.WALL ? 1 : 0) +
						(map[i - 1] == Terrain.WALL ? 1 : 0) +
						(map[i + width] == Terrain.WALL ? 1 : 0) +
						(map[i - width] == Terrain.WALL ? 1 : 0);

				if (Random.Int(16) < count * count) {
					map[i] = Terrain.EMPTY_DECO;
				}
			}
		}

		placeEntranceSign();
		placeBarrels(Random.Int(2));
	}

	@Override
	protected void createMobs() {
		super.createMobs();

		Ghost.Quest.spawn(this);

		if (ModdingMode.isHalloweenEvent()) {
			if (Dungeon.depth == 2) {
				ScarecrowNPC.spawn(this);
			}
		}

	}

	@Override
	protected void createItems() {
		if (Dungeon.dewVial && Random.Int(4 - Dungeon.depth) == 0) {
			addItemToSpawn(new DewVial());
			Dungeon.dewVial = false;
		}

		super.createItems();
	}

	@Override
	public void addVisuals(Scene scene) {
		super.addVisuals(scene);
		addVisuals(this, scene);
	}

	public static void addVisuals(Level level, Scene scene) {
		for (int i = 0; i < level.getLength(); i++) {
			if (level.map[i] == Terrain.WALL_DECO) {
				scene.add(new WaterSink(i));
			}
		}
	}

	@Override
	public String tileName(int tile) {
		switch (tile) {
			case Terrain.WATER:
                return StringsManager.getVar(R.string.Sewer_TileWater);
            default:
				return super.tileName(tile);
		}
	}

	@Override
	public String tileDesc(int tile) {
		switch (tile) {
			case Terrain.EMPTY_DECO:
                return StringsManager.getVar(R.string.Sewer_TileDescDeco);
            case Terrain.BOOKSHELF:
                return StringsManager.getVar(R.string.Sewer_TileDescBookshelf);
            default:
				return super.tileDesc(tile);
		}
	}

	@Override
	public int objectsKind() {
		return 0;
	}
}
