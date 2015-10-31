package com.nyrds.pixeldungeon.utils;

import com.nyrds.pixeldungeon.spiders.levels.SpiderLevel;
import com.watabou.pixeldungeon.levels.CavesBossLevel;
import com.watabou.pixeldungeon.levels.CavesLevel;
import com.watabou.pixeldungeon.levels.CityBossLevel;
import com.watabou.pixeldungeon.levels.CityLevel;
import com.watabou.pixeldungeon.levels.DeadEndLevel;
import com.watabou.pixeldungeon.levels.HallsBossLevel;
import com.watabou.pixeldungeon.levels.HallsLevel;
import com.watabou.pixeldungeon.levels.LastLevel;
import com.watabou.pixeldungeon.levels.LastShopLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.PrisonBossLevel;
import com.watabou.pixeldungeon.levels.PrisonLevel;
import com.watabou.pixeldungeon.levels.SewerBossLevel;
import com.watabou.pixeldungeon.levels.SewerLevel;

public enum LevelKind {
	
	SEWER_LEVEL(SewerLevel.class),
	SEWER_BOSS_LEVEL(SewerBossLevel.class),
	PRISON_LEVEL(PrisonLevel.class),
	PRISON_BOSS_LEVEL(PrisonBossLevel.class),
	CAVES_LEVEL(CavesLevel.class),
	CAVES_BOSS_LEVEL(CavesBossLevel.class),
	CITY_LEVEL(CityLevel.class),
	CITY_BOSS_LEVEL(CityBossLevel.class),
	HALLS_LEVEL(HallsLevel.class),
	HALLS_BOSS_LEVEL(HallsBossLevel.class),
	SPIDER_LEVEL(SpiderLevel.class),
	LAST_LEVEL(LastLevel.class),
	LAST_SHOP_LEVEL(LastShopLevel.class),
	DEAD_END_LEVEL(DeadEndLevel.class);
	
	
	private Class<? extends Level> levelClass;
	
	LevelKind(Class<? extends Level> levelClass) {
		this.levelClass = levelClass;
	}
	
	Level create() {
		try {
			return levelClass.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
