package com.nyrds.pixeldungeon.items.common.armor;

import com.nyrds.pixeldungeon.mechanics.buffs.Necrotism;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.utils.GLog;

public class NecromancerArmor extends ClassArmor {


	public NecromancerArmor() {
		image = 22;
	}

	public String desc() {
		return info2;
	}

	@Override
	public String special() {
		return "Necrotism_ACSpecial";
	}

	@Override
	public void doSpecial() {
		getCurUser().spend( Actor.TICK );
		getCurUser().getSprite().operate( getCurUser().getPos() );
		getCurUser().busy();

		Buff.affect( getCurUser(), Necrotism.class ).set(Necrotism.duration, 1);

		getCurUser().getSprite().burst( 0x6935a5, 3 );
		Sample.INSTANCE.play( Assets.SND_READ );
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.NECROMANCER) {
			return super.doEquip( hero );
		} else {
			GLog.w( Game.getVar(R.string.NecromancerArmor_NotNecromancer) );
			return false;
		}
	}
}