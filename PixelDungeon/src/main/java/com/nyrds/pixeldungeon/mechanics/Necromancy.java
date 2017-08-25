package com.nyrds.pixeldungeon.mechanics;

import com.nyrds.pixeldungeon.items.common.armor.NecromancerArmor;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.Deathling;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.Collection;

public class Necromancy {

	private static final String TXT_MAXIMUM_PETS  	   = Game.getVar(R.string.NecromancerRobe_PetLimitReached);
	private static final String TXT_NOT_ENOUGH_SOULS   = Game.getVar(R.string.Necromancy_NotEnoughSouls);
	private static final String TXT_SUMMON_DEATHLING   = Game.getVar(R.string.Necromancy_SummonDeathlingName);
	private static final String TXT_REINCARNATION   = Game.getVar(R.string.Necromancy_ReincarnationName);

	private static final int DEATHLING_COST = 5;

	private static final int ARMOR_LIMIT = 2;
	private static final int ROBES_LIMIT = 1;

	public static String SUMMON_DEATHLING = "SUMMON_DEATHLING";
	public static String REINCARNATION = "REINCARNATION";

	private static int getLimit(Item source, Hero hero){
		int limit;
		if (source instanceof NecromancerArmor){
			limit = ARMOR_LIMIT;
		} else {
			limit = ROBES_LIMIT;
		}
		if (hero.subClass == HeroSubClass.LICH){
			return limit * 2;
		}
		return limit;
	}

	private static String getLimitWarning(Item source, Hero hero){
		return Utils.format(TXT_MAXIMUM_PETS, getLimit(source, hero));
	}

	public static String notEnoughSouls (String spell) {
		if (spell.equals(SUMMON_DEATHLING)){
			return Utils.format(TXT_NOT_ENOUGH_SOULS, TXT_SUMMON_DEATHLING);
		}
		if (spell.equals(REINCARNATION)){
			return Utils.format(TXT_NOT_ENOUGH_SOULS, TXT_REINCARNATION);
		}
		return Utils.format(TXT_NOT_ENOUGH_SOULS, "Unknown spell!!!");
	}

	//TODO: Spells is down there. We need more spells

	public static void summonDeathling(Item source){
		Collection<Mob> pets = Dungeon.hero.getPets();
		Hero hero = Dungeon.hero;

		int n = 0;
		for (Mob mob : pets){
			if (mob.isAlive() && mob instanceof Deathling) {
				n++;
			}
		}

		if (n >= Necromancy.getLimit(source, hero)){
			GLog.w( Necromancy.getLimitWarning(source, hero) );
			return;
		}

		if(!hero.spendSoulPoints(DEATHLING_COST)){
			GLog.w( notEnoughSouls(SUMMON_DEATHLING) );
			return;
		}

		int spawnPos = Dungeon.level.getEmptyCellNextTo(hero.getPos());

		Wound.hit(hero);
		Buff.detach(hero, Sungrass.Health.class);

		if (Dungeon.level.cellValid(spawnPos)) {
			Mob pet = Mob.makePet(new Deathling(), hero);
			pet.setPos(spawnPos);
			Dungeon.level.spawnMob(pet);
		}

		hero.spend(1/hero.speed());
	}
}
