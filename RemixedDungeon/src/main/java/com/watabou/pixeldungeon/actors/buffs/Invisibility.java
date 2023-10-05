
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

public class Invisibility extends FlavourBuff {

	public static final float DURATION	= 15f;
	
	@Override
	public boolean attachTo(@NotNull Char target ) {
		if (super.attachTo( target )) {
			target.invisible++;
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		target.invisible--;
		super.detach();
	}
	
	@Override
	public int icon() {
		return BuffIndicator.INVISIBLE;
	}

	public static void dispel(@NotNull Char tgt) {
		if(tgt.visibleEnemies() > 0) {
			detach(tgt, Invisibility.class);
			detach(tgt, "Cloak");
		}
	}

	@Override
	public void attachVisual() {
		super.attachVisual();
        target.showStatus(CharSprite.POSITIVE, StringsManager.getVar(R.string.Char_StaInvisible));
	}

	@Override
	public CharSprite.State charSpriteStatus() {
		return CharSprite.State.INVISIBLE;
	}
}
