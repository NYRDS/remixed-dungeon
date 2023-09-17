
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.Packable;
import com.nyrds.platform.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;

import org.jetbrains.annotations.NotNull;

public class Shadows extends Invisibility {

	@Packable
	protected float left;

	@Override
	public boolean attachTo(@NotNull Char target ) {
		if (super.attachTo( target )) {
			Sample.INSTANCE.play( Assets.SND_MELD );
			Dungeon.observe();
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void detach() {
		super.detach();
		Dungeon.observe();
	}
	
	@Override
	public boolean act() {
		spend( TICK * 2 );

		if (target.isAlive()) {
			if (--left <= 0 || target.visibleEnemies() > 0) {
				detach();
			}
		} else {
			detach();
		}
		
		return true;
	}
	
	public void prolong() {
		left = 2;
	}
	
	@Override
	public int icon() {
		return BuffIndicator.SHADOWS;
	}

	@Override
	public CharSprite.State charSpriteStatus() {
		return CharSprite.State.INVISIBLE;
	}
}
