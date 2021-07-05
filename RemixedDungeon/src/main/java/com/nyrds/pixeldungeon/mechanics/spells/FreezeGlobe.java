package com.nyrds.pixeldungeon.mechanics.spells;

import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Frost;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.effects.particles.SnowParticle;
import com.watabou.pixeldungeon.mechanics.Ballistica;

import org.jetbrains.annotations.NotNull;

public class FreezeGlobe extends Spell{

	FreezeGlobe() {
		targetingType = SpellHelper.TARGET_CELL;
		magicAffinity = SpellHelper.AFFINITY_ELEMENTAL;

		level = 3;
		image = 3;
		spellCost = 4;
	}

	@Override
	public boolean cast(@NotNull Char chr, int cell){
		if(!Dungeon.level.cellValid(cell)) {
			return false;
		}
		boolean triggered = false;
		if(Ballistica.cast(chr.getPos(), cell, false, true) == cell) {
			Char ch = Actor.findChar( cell );
			if (ch != null) {
				ch.getSprite().emitter().burst( SnowParticle.FACTORY, 5 );
				ch.getSprite().burst( 0xFF99FFFF, 3 );

				Buff.affect( ch, Frost.class, Frost.duration( ch ) );
				Buff.affect( ch, Slow.class, Slow.duration( ch ) );
				Sample.INSTANCE.play( Assets.SND_SHATTER );
				triggered = true;
			}
			if(triggered) {
				castCallback(chr);
			}
			return true;
		}
		return false;
	}

	@Override
	public String texture(){
		return "spellsIcons/elemental.png";
	}
}
