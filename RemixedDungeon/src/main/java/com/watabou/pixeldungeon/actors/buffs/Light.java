
package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

public class Light extends FlavourBuff {

	public static final float DURATION	= 250f;
	
	@Override
	public boolean attachTo(@NotNull Char target ) {
		if (super.attachTo( target )) {
			target.observe();
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		super.detach();
		target.observe();
	}
	
	@Override
	public int icon() {
		return BuffIndicator.LIGHT;
	}

	@Override
	public CharSprite.State charSpriteStatus() {
		return CharSprite.State.ILLUMINATED;
	}
}
