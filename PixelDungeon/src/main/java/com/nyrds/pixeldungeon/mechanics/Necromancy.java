package com.nyrds.pixeldungeon.mechanics;

import com.nyrds.pixeldungeon.items.common.armor.NecromancerArmor;
import com.nyrds.pixeldungeon.items.common.armor.NecromancerRobe;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.Deathling;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.pixeldungeon.utils.Utils;

public class Necromancy {

	private static final String TXT_MAXIMUM_PETS   = Game.getVar(R.string.NecromancerRobe_PetAlreadyExists);
	private static final int ARMOR_LIMIT = 2;
	private static final int ROBES_LIMIT = 1;

	public static int getLimit(Item source){
		if (source instanceof NecromancerArmor){
			return ARMOR_LIMIT;
		}
		return ROBES_LIMIT;
	}

	public static String getLimitWarning(Item source){
		if (source instanceof NecromancerArmor){
			return Utils.format(TXT_MAXIMUM_PETS, ARMOR_LIMIT);
		}
		if (source instanceof NecromancerRobe){
			return Utils.format(TXT_MAXIMUM_PETS, ROBES_LIMIT);
		}
		return "<Warning> Summoning item class does not specified!";
	}

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
