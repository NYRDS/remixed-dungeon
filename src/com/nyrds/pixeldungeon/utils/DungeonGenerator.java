package com.nyrds.pixeldungeon.utils;

import java.util.HashMap;
import java.util.Map;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;

public class DungeonGenerator {
	private static Map<String, LevelKind> levelTypes = levelTypes(); 

	public static Position ascend(Position current) {
		Position ret = new Position();
		ret.levelDepth = current.levelDepth - 1;
		ret.levelKind  = depthToKind(ret.levelDepth);
		
		return current;
	}
	
	public static Position descend(Position current) {
		Position ret = new Position();
		ret.levelDepth = current.levelDepth + 1;
		ret.levelKind  = depthToKind(ret.levelDepth);
		
		return ret;
	}
	
	public static Level createLevel(Position pos) {
		if(!levelTypes.containsKey(pos.levelKind)) {
			GLog.w("Unknown level type: %s", pos.levelKind);
			pos.levelKind = "DeadEndLevel";
			return createLevel(pos);
		}
		return levelTypes.get(pos.levelKind).create();
	}
	
	private static Map<String, LevelKind> levelTypes() {
		Map<String, LevelKind> lmap = new HashMap<String, LevelKind>();
		
		lmap.put("SewerLevel",         LevelKind.SEWER_LEVEL);
		lmap.put("SewerBossLevel",     LevelKind.SEWER_BOSS_LEVEL);
		lmap.put("PrisonLevel",        LevelKind.PRISON_LEVEL);
		lmap.put("PrisonBossLevel",    LevelKind.PRISON_BOSS_LEVEL);
		lmap.put("CavesLevel",         LevelKind.CAVES_LEVEL);
		lmap.put("CavesBossLevel",     LevelKind.CAVES_BOSS_LEVEL);
		lmap.put("CityLevel",          LevelKind.CITY_LEVEL);
		lmap.put("CityBossLevel",      LevelKind.CITY_BOSS_LEVEL);
		lmap.put("HallsLevels",        LevelKind.HALLS_LEVEL);
		lmap.put("HallsBossLevels",    LevelKind.HALLS_BOSS_LEVEL);
		lmap.put("LastShopLevel",      LevelKind.LAST_SHOP_LEVEL);
		lmap.put("LastLevel",          LevelKind.LAST_LEVEL);
		lmap.put("SpiderLevel",        LevelKind.SPIDER_LEVEL);
		
		return lmap;
	}


	private static String depthToKind(int depth) {
		
		switch (depth) {
		case 1:
		case 2:
		case 3:
		case 4:
			return "SewerLevel";
		case 5:
			return "SewerBossLevel";
		case 6:
		case 7:
		case 8:
		case 9:
			return "PrisonLevel";
		case 10:
			return "PrisonBossLevel";
		case 11:
		case 12:
		case 13:
		case 14:
			return "CavesLevel";
		case 15:
			return "CavesBossLevel";
		case 16:
		case 17:
		case 18:
		case 19:
			return "CityLevel";
		case 20:
			return "CityBossLevel";
		case 21:
			return "LastShopLevel";
		case 22:
		case 23:
		case 24:
			return "HallsLevel";
		case 25:
			return "HallsBossLevel";
		case 26:
			return "LastLevel";
		default:
			return "DeadEndLevel";
		}
	}
	
	
	
}
