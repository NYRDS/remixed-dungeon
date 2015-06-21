package com.nyrds.pixeldungeon.mobs.spiders;

import android.util.SparseBooleanArray;

import com.nyrds.pixeldungeon.mobs.spiders.sprites.SpiderEggSprite;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Mob;

public class SpiderEgg extends Mob {

	private static SparseBooleanArray eggsLaid = new SparseBooleanArray();

	public SpiderEgg() {
		spriteClass = SpiderEggSprite.class;

		hp(ht(2));
		defenseSkill = 0;
		baseSpeed = 0f;

		EXP = 0;
		maxLvl = 9;

		postpone(20);
	}

	public static void layEgg(int pos) {
		eggsLaid.append(pos, true);
		SpiderSpawner.spawnEgg(Dungeon.level, pos);
	}

	public static boolean eggLaid(int pos) {
		return eggsLaid.get(pos, false);
	}

	@Override
	protected boolean act() {
		super.act();

		SpiderSpawner.spawnRandomSpider(Dungeon.level, pos);

		remove();
		eggsLaid.delete(pos);

		return true;
	}
}
