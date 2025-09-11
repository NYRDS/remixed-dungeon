
package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Sleeping;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.items.common.RatKingCrown;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Fraction;

import org.jetbrains.annotations.NotNull;

public class RatKing extends NPC {

	@Packable
	public int anger = 0;
	
	public RatKing() {
		setState(MobAi.getStateByClass(Sleeping.class));
		baseDefenseSkill = 20;
		baseAttackSkill  = 15;
		dmgMin = 4;
		dmgMax = 10;
		dr = 5;
		
		hp(ht(30));
		expForKill = 1;

		collect(new RatKingCrown() );
	}

	@Override
	public boolean friendly(@NotNull Char chr){
		if(chr instanceof Hero) {
			return anger < 2;
		} else {
			return super.friendly(chr);
		}
	}
	
	@Override
	public float speed() {
		return 2f;
	}

	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src ) {
		if(friendly(Dungeon.hero)){
			anger=2;
		} else {
			super.damage(dmg, src);
		}
	}
	
	@Override
	public boolean add(Buff buff ) {
		if (!friendly(Dungeon.hero)) {
			return super.add(buff);
		}
        return false;
    }
	
	@Override
	public boolean reset() {
		return true;
	}
	
	@Override
	public boolean interact(final Char hero) {
		getSprite().turnTo( getPos(), hero.getPos() );
		
		if (!friendly(hero)) {
			return false;
		}
		
		if (getState() instanceof Sleeping) {
			notice();
            say(StringsManager.getVar(R.string.RatKing_Info1));
			setState(MobAi.getStateByClass(Wandering.class));
		} else {
			anger++;
			if(friendly(hero)) {
                say(StringsManager.getVar(R.string.RatKing_Info2));
			} else {
				setFraction(Fraction.DUNGEON);

				setState(MobAi.getStateByClass(Hunting.class));
                yell(StringsManager.getVar(R.string.RatKing_Info3));
			}
		}
		return true;
	}
	
	@Override
	public void die(@NotNull NamedEntityKind cause) {
        say(StringsManager.getVar(R.string.RatKing_Died));
		super.die(cause);
	}

}
