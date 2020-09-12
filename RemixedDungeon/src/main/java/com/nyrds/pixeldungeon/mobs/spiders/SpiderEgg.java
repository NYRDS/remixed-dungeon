package com.nyrds.pixeldungeon.mobs.spiders;

import android.util.SparseBooleanArray;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.mobs.common.MobSpawner;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Mob;

public class SpiderEgg extends Mob {

	private static SparseBooleanArray eggsLaid = new SparseBooleanArray();

	public SpiderEgg() {
		hp(ht(2));
		defenseSkill = 0;
		baseSpeed = 0f;

		exp = 0;
		maxLvl = 9;

		postpone(20);
		
		loot(Treasury.Category.SEED, 0.2f);

		movable = false;
	}

	public static void lay(int pos) {
		eggsLaid.append(pos, true);
		SpiderSpawner.spawnEgg(Dungeon.level, pos);
	}

	public static boolean laid(int pos) {
		return eggsLaid.get(pos, false);
	}
	
	@Override
    public boolean act() {
		super.act();

		MobSpawner.spawnRandomMob(Dungeon.level,getPos());

		remove();
		eggsLaid.delete(getPos());

		return true;
	}

	@Override
	public boolean canBePet() {
		return false;
	}
}
