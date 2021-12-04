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

public class Terrain {

	public static final int CHASM = 0;
	public static final int EMPTY = 1;
	public static final int GRASS = 2;
	public static final int EMPTY_WELL = 3; //deprecated
	public static final int WALL = 4;
	public static final int DOOR = 5;
	public static final int OPEN_DOOR = 6;
	public static final int ENTRANCE = 7;
	public static final int EXIT = 8;
	public static final int EMBERS = 9;
	public static final int LOCKED_DOOR = 10;
	public static final int PEDESTAL = 11; //deprecated
	public static final int WALL_DECO = 12;
	public static final int BARRICADE = 13; // deprecated
	public static final int EMPTY_SP = 14;
	public static final int HIGH_GRASS = 15;
	public static final int EMPTY_DECO = 24;
	public static final int LOCKED_EXIT = 25;
	public static final int UNLOCKED_EXIT = 26;
	public static final int SIGN = 29; //deprecated
	public static final int WELL = 34; //deprecated
	public static final int STATUE = 35; //deprecated
	public static final int STATUE_SP = 36; //deprecated
	public static final int BOOKSHELF = 41; //Alternative wall actually
	public static final int ALCHEMY = 42; //deprecated
	public static final int CHASM_FLOOR = 43;
	public static final int CHASM_FLOOR_SP = 44;
	public static final int CHASM_WALL = 45;
	public static final int CHASM_WATER = 46;

	public static final int SECRET_DOOR = 16;

	//all deprecated
	public static final int TOXIC_TRAP = 17;
	public static final int SECRET_TOXIC_TRAP = 18;
	public static final int FIRE_TRAP = 19;
	public static final int SECRET_FIRE_TRAP = 20;
	public static final int PARALYTIC_TRAP = 21;
	public static final int SECRET_PARALYTIC_TRAP = 22;
	public static final int INACTIVE_TRAP = 23;
	public static final int POISON_TRAP = 27;
	public static final int SECRET_POISON_TRAP = 28;
	public static final int ALARM_TRAP = 30;
	public static final int SECRET_ALARM_TRAP = 31;
	public static final int LIGHTNING_TRAP = 32;
	public static final int SECRET_LIGHTNING_TRAP = 33;
	public static final int GRIPPING_TRAP = 37;
	public static final int SECRET_GRIPPING_TRAP = 38;
	public static final int SUMMONING_TRAP = 39;
	public static final int SECRET_SUMMONING_TRAP = 40;

	public static final int WATER_TILES = 48;
	public static final int WATER = 63;

	public static int[] SECRET_TRAPS = {SECRET_FIRE_TRAP,
										SECRET_PARALYTIC_TRAP,
										SECRET_TOXIC_TRAP,
										SECRET_POISON_TRAP,
										SECRET_ALARM_TRAP,
										SECRET_LIGHTNING_TRAP,
										SECRET_GRIPPING_TRAP,
										SECRET_SUMMONING_TRAP};

	public static int discover(int terr) {
		switch (terr) {
			case SECRET_DOOR:
				return DOOR;
			case SECRET_FIRE_TRAP:
				return FIRE_TRAP;
			case SECRET_PARALYTIC_TRAP:
				return PARALYTIC_TRAP;
			case SECRET_TOXIC_TRAP:
				return TOXIC_TRAP;
			case SECRET_POISON_TRAP:
				return POISON_TRAP;
			case SECRET_ALARM_TRAP:
				return ALARM_TRAP;
			case SECRET_LIGHTNING_TRAP:
				return LIGHTNING_TRAP;
			case SECRET_GRIPPING_TRAP:
				return GRIPPING_TRAP;
			case SECRET_SUMMONING_TRAP:
				return SUMMONING_TRAP;
			default:
				return terr;
		}
	}
}
