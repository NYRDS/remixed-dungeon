/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Invisibility;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.scrolls.BlankScroll;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.utils.GLog;

public class PotionOfInvisibility extends UpgradablePotion {

	private static final float ALPHA	= 0.4f;
	
	@Override
	protected void apply( Hero hero ) {
		setKnown();
		Buff.affect( hero, Invisibility.class, (float) (Invisibility.DURATION * qualityFactor()));
		GLog.i(Game.getVar(R.string.PotionOfInvisibility_Apply));
		Sample.INSTANCE.play( Assets.SND_MELD );
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.PotionOfInvisibility_Info);
	}

	@Override
	public int basePrice() {
		return 40;
	}

	public static void melt(Char ch ) {
		if (ch.getSprite().hasParent()) {
			ch.getSprite().getParent().add( new AlphaTweener( ch.getSprite(), ALPHA, 0.4f ) );
		} else {
			ch.getSprite().alpha( ALPHA );
		}
	}
	
	@Override
	protected void moistenScroll(Scroll scroll) {
		int quantity = detachMoistenItems(scroll, (int) (3*qualityFactor()));
		
		GLog.i(TXT_RUNE_DISAPPEARED, scroll.name());
		
		moistenEffective();
		
		BlankScroll moistenScroll = new BlankScroll();
		moistenScroll.quantity(quantity);
		getCurUser().collect(moistenScroll);
	}
}
