package com.nyrds.pixeldungeon.items.common.armor;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.Deathling;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Wound;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.HashSet;

public class NecromancerArmor extends ClassArmor {

	private static final String TXT_NOT_NECROMANCER = Game.getVar(R.string.NecromancerArmor_NotNecromancer);
	private static final String AC_SPECIAL = Game.getVar(R.string.NecromancerArmor_ACSpecial);

	public HashSet<Mob> pets   = new HashSet<>();

	public NecromancerArmor() {
		image = 22;
	}	
	
	@Override
	public String special() {
		return AC_SPECIAL;
	}
	
	@Override
	public void doSpecial() {
		Char ch = getCurUser();

		Wound.hit(ch);
		ch.damage(4 + Dungeon.hero.lvl() * 2, this);
		Buff.detach(ch, Sungrass.Health.class);

		for (int i =0 ; i < 4; i++){
			int spawnPos = Dungeon.level.getEmptyCellNextTo(ch.getPos());
			if (Dungeon.level.cellValid(spawnPos)) {
				Mob pet = Mob.makePet(new Deathling(), getCurUser());
				pet.setPos(spawnPos);
				pets.add(pet);
				Dungeon.level.spawnMob(pet);
			}
		}
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.NECROMANCER) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_NECROMANCER );
			return false;
		}
	}
}