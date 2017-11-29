package com.nyrds.pixeldungeon.items.common.armor;

import com.nyrds.pixeldungeon.mechanics.buffs.Necrotism;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.particles.BloodParticle;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.HashSet;

public class NecromancerArmor extends ClassArmor {


	private static final String TXT_NOT_NECROMANCER = Game.getVar(R.string.NecromancerArmor_NotNecromancer);
	private static final String AC_SPECIAL = Game.getVar(R.string.Necromancer_ACSpecial);

	public HashSet<Mob> pets   = new HashSet<>();

	public NecromancerArmor() {
		image = 22;
	}

	public String desc() {
		return info2;
	}

	@Override
	public String special() {
		return AC_SPECIAL;
	}

	@Override
	public void doSpecial() {
		getCurUser().spend( Actor.TICK );
		getCurUser().getSprite().operate( getCurUser().getPos() );
		getCurUser().busy();

		Buff.affect( getCurUser(), Necrotism.class ).set(Necrotism.duration);

		getCurUser().getSprite().centerEmitter().start( BloodParticle.FACTORY, 0.25f, 7 );
		Sample.INSTANCE.play( Assets.SND_READ );
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