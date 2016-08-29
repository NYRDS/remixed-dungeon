package com.nyrds.pixeldungeon.mechanics;

import com.nyrds.pixeldungeon.mobs.common.Deathling;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.plants.Sungrass;

public class Necromancy {

	public static void summonDeathling(Item source){
		Hero hero = Dungeon.hero;
		int spawnPos = Dungeon.level.getEmptyCellNextTo(hero.getPos());

		Wound.hit(hero);
		hero.damage(4 + Dungeon.hero.lvl(), source);
		Buff.detach(hero, Sungrass.Health.class);

		if (Dungeon.level.cellValid(spawnPos)) {
			Mob pet = Mob.makePet(new Deathling(), hero);
			pet.setPos(spawnPos);
			Dungeon.level.spawnMob(pet);
		}
	}
}
