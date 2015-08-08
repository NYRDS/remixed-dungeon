package com.nyrds.pixeldungeon.utils;

import java.util.HashMap;
import java.util.Map;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.items.SpiderCharm;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;

public class DungeonGenerator {
	public static final String DEAD_END_LEVEL    = "DeadEndLevel";
	public static final String LAST_LEVEL        = "LastLevel";
	public static final String LAST_SHOP_LEVEL   = "LastShopLevel";
	public static final String HALLS_BOSS_LEVELS = "HallsBossLevels";
	public static final String HALLS_LEVELS      = "HallsLevels";
	public static final String CITY_BOSS_LEVEL   = "CityBossLevel";
	public static final String CITY_LEVEL        = "CityLevel";
	public static final String CAVES_BOSS_LEVEL  = "CavesBossLevel";
	public static final String CAVES_LEVEL       = "CavesLevel";
	public static final String PRISON_BOSS_LEVEL = "PrisonBossLevel";
	public static final String PRISON_LEVEL      = "PrisonLevel";
	public static final String SEWER_BOSS_LEVEL  = "SewerBossLevel";
	public static final String SEWER_LEVEL       = "SewerLevel";
	public static final String SPIDER_LEVEL      = "SpiderLevel";
	
	private static Map<String, LevelKind> levelTypes = levelTypes(); 

	public static Position ascend(Position current) {
		Position next = new Position();
		next.levelDepth = current.levelDepth - 1;
		next.levelKind  = depthToKind(next.levelDepth);
		
		if(current.levelKind.equals(SPIDER_LEVEL) && next.levelDepth !=5) {
			next.levelKind = SPIDER_LEVEL;
		}
		
		return next;
	}
	
	public static Position descend(Position current) {
		Position next = new Position();
		next.levelDepth = current.levelDepth + 1;
		next.levelKind  = depthToKind(next.levelDepth);
		
		if (next.levelDepth == 6) {
			if (Dungeon.hero.belongings.ring1 instanceof SpiderCharm
					|| Dungeon.hero.belongings.ring2 instanceof SpiderCharm) {
				next.levelKind = SPIDER_LEVEL;
				next.xs = 16 + (next.levelDepth-6) * 16;
				next.ys = 16 + (next.levelDepth-6) * 16;
			}
		}
		
		if(current.levelKind.equals(SPIDER_LEVEL)) {
			next.levelKind = SPIDER_LEVEL;
		}
		
		return next;
	}
	
	public static Level createLevel(Position pos) {
		if(!levelTypes.containsKey(pos.levelKind)) {
			GLog.w("Unknown level type: %s", pos.levelKind);
			pos.levelKind = DEAD_END_LEVEL;
			return createLevel(pos);
		}
		return levelTypes.get(pos.levelKind).create();
	}
	
	private static Map<String, LevelKind> levelTypes() {
		Map<String, LevelKind> lmap = new HashMap<String, LevelKind>();
		
		lmap.put(SEWER_LEVEL,          LevelKind.SEWER_LEVEL);
		lmap.put(SEWER_BOSS_LEVEL,     LevelKind.SEWER_BOSS_LEVEL);
		lmap.put(PRISON_LEVEL,         LevelKind.PRISON_LEVEL);
		lmap.put(PRISON_BOSS_LEVEL,    LevelKind.PRISON_BOSS_LEVEL);
		lmap.put(CAVES_LEVEL,          LevelKind.CAVES_LEVEL);
		lmap.put(CAVES_BOSS_LEVEL,     LevelKind.CAVES_BOSS_LEVEL);
		lmap.put(CITY_LEVEL,           LevelKind.CITY_LEVEL);
		lmap.put(CITY_BOSS_LEVEL,      LevelKind.CITY_BOSS_LEVEL);
		lmap.put(HALLS_LEVELS,         LevelKind.HALLS_LEVEL);
		lmap.put(HALLS_BOSS_LEVELS,    LevelKind.HALLS_BOSS_LEVEL);
		lmap.put(LAST_SHOP_LEVEL,      LevelKind.LAST_SHOP_LEVEL);
		lmap.put(LAST_LEVEL,           LevelKind.LAST_LEVEL);
		lmap.put(SPIDER_LEVEL,         LevelKind.SPIDER_LEVEL);
		lmap.put(DEAD_END_LEVEL,       LevelKind.DEAD_END_LEVEL);
		
		return lmap;
	}

	private static String depthToKind(int depth) {
		
		switch (depth) {
		case 1:
		case 2:
		case 3:
		case 4:
			return SEWER_LEVEL;
		case 5:
			return SEWER_BOSS_LEVEL;
		case 6:
		case 7:
		case 8:
		case 9:
			return PRISON_LEVEL;
		case 10:
			return PRISON_BOSS_LEVEL;
		case 11:
		case 12:
		case 13:
		case 14:
			return CAVES_LEVEL;
		case 15:
			return CAVES_BOSS_LEVEL;
		case 16:
		case 17:
		case 18:
		case 19:
			return CITY_LEVEL;
		case 20:
			return CITY_BOSS_LEVEL;
		case 21:
			return LAST_SHOP_LEVEL;
		case 22:
		case 23:
		case 24:
			return HALLS_LEVELS;
		case 25:
			return HALLS_BOSS_LEVELS;
		case 26:
			return LAST_LEVEL;
		default:
			return DEAD_END_LEVEL;
		}
	}
	
	
	
}
