package com.watabou.pixeldungeon.levels;

/**
 * Created by mike on 22.02.2016.
 */
public class TerrainFlags {
	public static final int   PASSABLE     = 0x01;
	public static final int   LOS_BLOCKING = 0x02;
	public static final int   FLAMMABLE    = 0x04;
	public static final int   SECRET       = 0x08;
	public static final int   SOLID        = 0x10;
	public static final int   AVOID        = 0x20;
	public static final int   LIQUID       = 0x40;
	public static final int   PIT          = 0x80;
	public static final int   UNSTITCHABLE = 0x100;
	public static final int   TRAP         = 0x200;
	public static final int   DEPRECATED   = 0x400;
	public static final int[] flags        = new int[64];

	public static boolean is(int terrain, int flag) {
		return (flags[terrain] & flag) == flag;
	}

	static {
		flags[Terrain.CHASM] = AVOID | PIT | UNSTITCHABLE;
		flags[Terrain.EMPTY] = PASSABLE;
		flags[Terrain.GRASS] = PASSABLE | FLAMMABLE;
		flags[Terrain.EMPTY_WELL] = PASSABLE | DEPRECATED;
		flags[Terrain.WATER] = PASSABLE | LIQUID | UNSTITCHABLE;
		flags[Terrain.WALL] = LOS_BLOCKING | SOLID | UNSTITCHABLE;
		flags[Terrain.DOOR] = PASSABLE | LOS_BLOCKING | FLAMMABLE | SOLID | UNSTITCHABLE;
		flags[Terrain.OPEN_DOOR] = PASSABLE | FLAMMABLE | UNSTITCHABLE;
		flags[Terrain.ENTRANCE] = PASSABLE/* | SOLID*/;
		flags[Terrain.EXIT] = PASSABLE;
		flags[Terrain.EMBERS] = PASSABLE;
		flags[Terrain.LOCKED_DOOR] = LOS_BLOCKING | SOLID | UNSTITCHABLE;
		flags[Terrain.PEDESTAL] = PASSABLE | UNSTITCHABLE | DEPRECATED;
		flags[Terrain.WALL_DECO] = flags[Terrain.WALL];
		flags[Terrain.BARRICADE] = FLAMMABLE | SOLID | LOS_BLOCKING | DEPRECATED;
		flags[Terrain.EMPTY_SP] = flags[Terrain.EMPTY] | UNSTITCHABLE;
		flags[Terrain.HIGH_GRASS] = PASSABLE | LOS_BLOCKING | FLAMMABLE;
		flags[Terrain.EMPTY_DECO] = flags[Terrain.EMPTY];
		flags[Terrain.LOCKED_EXIT] = SOLID;
		flags[Terrain.UNLOCKED_EXIT] = PASSABLE;
		flags[Terrain.SIGN] = PASSABLE | FLAMMABLE | DEPRECATED;
		flags[Terrain.WELL] = AVOID;
		flags[Terrain.STATUE] = SOLID;
		flags[Terrain.STATUE_SP] = flags[Terrain.STATUE] | UNSTITCHABLE | DEPRECATED;
		flags[Terrain.BOOKSHELF] = flags[Terrain.BARRICADE] | UNSTITCHABLE | DEPRECATED;
		flags[Terrain.ALCHEMY] = PASSABLE | DEPRECATED;

		flags[Terrain.CHASM_WALL] = flags[Terrain.CHASM];
		flags[Terrain.CHASM_FLOOR] = flags[Terrain.CHASM];
		flags[Terrain.CHASM_FLOOR_SP] = flags[Terrain.CHASM];
		flags[Terrain.CHASM_WATER] = flags[Terrain.CHASM];

		flags[Terrain.SECRET_DOOR] = flags[Terrain.WALL] | SECRET | UNSTITCHABLE;
		flags[Terrain.TOXIC_TRAP] = AVOID | TRAP | DEPRECATED;
		flags[Terrain.SECRET_TOXIC_TRAP] = flags[Terrain.EMPTY] | SECRET | TRAP | DEPRECATED;
		flags[Terrain.FIRE_TRAP] = AVOID | TRAP | DEPRECATED;
		flags[Terrain.SECRET_FIRE_TRAP] = flags[Terrain.EMPTY] | SECRET | TRAP | DEPRECATED;
		flags[Terrain.PARALYTIC_TRAP] = AVOID | TRAP | DEPRECATED;
		flags[Terrain.SECRET_PARALYTIC_TRAP] = flags[Terrain.EMPTY] | SECRET | TRAP | DEPRECATED;
		flags[Terrain.POISON_TRAP] = AVOID | TRAP | DEPRECATED;
		flags[Terrain.SECRET_POISON_TRAP] = flags[Terrain.EMPTY] | SECRET | TRAP | DEPRECATED;
		flags[Terrain.ALARM_TRAP] = AVOID | TRAP | DEPRECATED;
		flags[Terrain.SECRET_ALARM_TRAP] = flags[Terrain.EMPTY] | SECRET | TRAP | DEPRECATED;
		flags[Terrain.LIGHTNING_TRAP] = AVOID | TRAP | DEPRECATED;
		flags[Terrain.SECRET_LIGHTNING_TRAP] = flags[Terrain.EMPTY] | SECRET | TRAP | DEPRECATED;
		flags[Terrain.GRIPPING_TRAP] = AVOID | TRAP | DEPRECATED;
		flags[Terrain.SECRET_GRIPPING_TRAP] = flags[Terrain.EMPTY] | SECRET | TRAP | DEPRECATED;
		flags[Terrain.SUMMONING_TRAP] = AVOID | TRAP | DEPRECATED;
		flags[Terrain.SECRET_SUMMONING_TRAP] = flags[Terrain.EMPTY] | SECRET | TRAP | DEPRECATED;
		flags[Terrain.INACTIVE_TRAP] = flags[Terrain.EMPTY] | DEPRECATED;

		for (int i = Terrain.WATER_TILES; i < Terrain.WATER_TILES + 16; i++) {
			flags[i] = flags[Terrain.WATER];
		}
	}

}
