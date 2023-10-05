
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class Fury extends Buff {
	
	public static float LEVEL	= 0.4f;
	
	@Override
	public boolean act() {
		if (target.hp() > target.ht() * LEVEL) {
			detach();
		}
		
		spend( TICK * 10 );
		
		return true;
	}

	@Override
	public boolean attachTo(@NotNull Char target) {
		if (CharUtils.isVisible(target)) {
			GLog.w(StringsManager.getVar(R.string.Brute_Enraged), target.getName() );
			target.showStatus( CharSprite.NEGATIVE, StringsManager.getVar(R.string.Brute_StaEnraged));
		}
		return super.attachTo(target);
	}

	@Override
	public int attackProc(Char attacker, Char defender, int damage) {
		return damage * 2;
	}

	@Override
	public int icon() {
		return BuffIndicator.FURY;
	}
}
