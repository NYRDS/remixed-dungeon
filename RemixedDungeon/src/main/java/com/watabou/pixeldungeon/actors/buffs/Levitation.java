
package com.watabou.pixeldungeon.actors.buffs;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

public class Levitation extends FlavourBuff {

	public static final float DURATION	= 20f;
	
	@Override
	public boolean attachTo(@NotNull Char target ) {
		if (super.attachTo( target )) {
			Roots.detach( target, Roots.class );
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		super.detach();
		if(Dungeon.level != null) {
			Dungeon.level.press(target.getPos(), target);
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.LEVITATION;
	}

	@Override
	public CharSprite.State charSpriteStatus() {
		return CharSprite.State.LEVITATING;
	}
}
