
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Doom;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.PoisonParticle;
import com.watabou.pixeldungeon.items.rings.RingOfElements.Resistance;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

public class Poison extends Buff implements Doom {

	@Override
	public int icon() {
		return BuffIndicator.POISON;
	}

	@Override
	public void charAct() {
		float timeLeft = cooldown();
		target.damage( (int)(timeLeft / 3) + 1, this );
	}

	@Override
	public boolean act() {
		detach();
		return true;
	}

	public static float durationFactor(Char ch ) {
		if(ch==null) { //primary to mask bug in Remixed Additions
			return 1;
		}

		Resistance r = ch.buff( Resistance.class );
		return r != null ? r.durationFactor() : 1;
	}

	@Override
	public void onHeroDeath() {
		Badges.validateDeathFromPoison();
		
		Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.POISON), Dungeon.depth ) );
        GLog.n(StringsManager.getVar(R.string.Poison_Death));
	}

	@Override
	public void attachVisual() {
		CellEmitter.center(target.getPos()).burst(PoisonParticle.SPLASH, 5);
        target.showStatus(CharSprite.NEGATIVE, StringsManager.getVar(R.string.Char_StaPoisoned));
	}
}
