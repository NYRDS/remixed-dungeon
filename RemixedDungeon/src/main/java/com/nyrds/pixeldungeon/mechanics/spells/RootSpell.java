package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.effects.particles.EarthParticle;

import org.jetbrains.annotations.NotNull;

public class RootSpell extends Spell{

	RootSpell() {
		targetingType = SpellHelper.TARGET_CHAR_NOT_SELF;
		magicAffinity = SpellHelper.AFFINITY_ELEMENTAL;

		level = 2;
		image = 2;
		spellCost = 2;
	}

	@Override
	public boolean cast(@NotNull Char chr, @NotNull Char target){

		if(target.valid()) {
			target.getSprite().emitter().burst( EarthParticle.FACTORY, 5 );
			target.getSprite().burst( 0xFF99FFFF, 3 );

			Buff.prolong( target, Roots.class, 10 );
			Sample.INSTANCE.play( Assets.SND_PUFF );

			castCallback(chr);
			return true;
		}
		return false;
	}

	@Override
	public String texture(){
		return "spellsIcons/elemental.png";
	}
}
