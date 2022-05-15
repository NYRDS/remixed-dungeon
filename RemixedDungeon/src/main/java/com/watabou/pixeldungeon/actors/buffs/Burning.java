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
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.Fire;
import com.watabou.pixeldungeon.actors.hero.Doom;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Effects;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.RingOfElements.Resistance;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Burning extends Buff implements Doom {

	private static final float DURATION = 8f;

	@Packable
	private float left;

	class burnItem implements ItemAction {
		public Item act(Item srcItem){
			return srcItem.burn(target.getPos());
		}
		public void carrierFx(){
			Effects.burnFX( target.getPos() );
		}
		@Override
		public String actionText(Item srcItem) {
            return Utils.format(R.string.Burning_Burns, srcItem.toString());
		}
	}
		
	@Override
	public boolean act() {
		
		if (target.isAlive()) {
			
			if (target instanceof Hero) {
				Buff.prolong( target, Light.class, TICK * 1.01f );
			}
			int bonusDamage = Dungeon.depth / 2;
			target.damage( Random.Int( 1 + bonusDamage, 5 + bonusDamage ), this );
			
			applyToCarriedItems(new burnItem());
		} else {
			detach();
		}

		final Level level = Dungeon.level;

		if (level.flammable[target.getPos()]) {
			GameScene.add( Blob.seed( target.getPos(), 4, Fire.class ) );
		}
		
		spend( TICK );
		left -= TICK;
		
		if (left <= 0 ||
			Random.Float() > (2 + (float)target.hp() / target.ht()) / 3 ||
			(level.water[target.getPos()] && !target.isFlying())) {
			
			detach();
		}
		
		return true;
	}
	
	public void reignite( Char ch ) {
		left = duration( ch );
	}
	
	@Override
	public int icon() {
		return BuffIndicator.FIRE;
	}

	public static float duration( Char ch ) {
		Resistance r = ch.buff( Resistance.class );
		return r != null ? r.durationFactor() * DURATION : DURATION;
	}

	@Override
	public void onDeath() {
		
		Badges.validateDeathFromFire();
		
		Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.BURNING), Dungeon.depth ) );
        GLog.n(StringsManager.getVar(R.string.Burning_Death));
	}

	@Override
	public CharSprite.State charSpriteStatus() {
		return CharSprite.State.BURNING;
	}
}
