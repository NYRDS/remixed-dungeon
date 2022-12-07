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
package com.watabou.pixeldungeon.plants;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Barkskin;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.particles.EarthParticle;
import com.watabou.pixeldungeon.items.food.Food;
import com.watabou.pixeldungeon.items.potions.PotionOfParalyticGas;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

public class Earthroot extends Plant {

	private static final String TXT_NAME = Game.getVar(R.string.Earthroot_Name);
	private static final String TXT_DESC = Game.getVar(R.string.Earthroot_Desc);
	
	public Earthroot() {
		image = 5;
		plantName = TXT_NAME;
	}

	public void effect(int pos, Char ch) {
		if (ch != null) {
			Buff.affect(ch, Armor.class).level = ch.ht();
		}

		if (Dungeon.visible[pos]) {
			CellEmitter.bottom(pos).start(EarthParticle.FACTORY, 0.05f, 8);
			Camera.main.shake(1, 0.4f);
		}
	}
	
	@Override
	public String desc() {
		return TXT_DESC;
	}
	
	public static class Seed extends Plant.Seed {
		{
			plantName = TXT_NAME;
			
			name = Utils.format(TXT_SEED, plantName);
			image = ItemSpriteSheet.SEED_EARTHROOT;
			
			plantClass = Earthroot.class;
			alchemyClass = PotionOfParalyticGas.class;
		}
		
		@Override
		public String desc() {
			return TXT_DESC;
		}
		
		@Override
		public void execute( Hero hero, String action ) {
			
			super.execute( hero, action );
			
			if (action.equals( Food.AC_EAT )) {
				Buff.affect(hero, Roots.class, 25);
				Buff.affect(hero, Barkskin.class).level(hero.effectiveSTR() / 4);
			}
		}
	}
	
	public static class Armor extends Buff {
		
		private static final float STEP = 1f;
		
		private int pos;
		private int level;
		
		@Override
		public boolean attachTo( Char target ) {
			pos = target.getPos();
			return super.attachTo( target );
		}
		
		@Override
		public boolean act() {
			if (target.getPos() != pos) {
				detach();
			}
			spend( STEP );
			return true;
		}
		
		public int absorb( int damage ) {
			if (damage >= level) {
				detach();
				return damage - level;
			} else {
				level -= damage;
				return 0;
			}
		}
		
		public void level( int value ) {
			if (level < value) {
				level = value;
			}
		}
		
		@Override
		public int icon() {
			return BuffIndicator.ARMOR;
		}
		
		@Override
		public String toString() {
			return Game.getVar(R.string.Earthroot_Buff);
		}
		
		private static final String POS		= "pos";
		private static final String LEVEL	= "level";
		
		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( POS, pos );
			bundle.put( LEVEL, level );
		}
		
		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			pos = bundle.getInt( POS );
			level = bundle.getInt( LEVEL );
		}
	}
}
