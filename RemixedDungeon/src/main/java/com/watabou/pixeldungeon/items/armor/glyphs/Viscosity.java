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
package com.watabou.pixeldungeon.items.armor.glyphs;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.Armor.Glyph;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.Glowing;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Viscosity extends Glyph {

	private static final Glowing PURPLE = new Glowing( 0x8844CC );
	
	@Override
	public int defenceProc(Armor armor, Char attacker, Char defender, int damage ) {

		if (damage == 0) {
			return 0;
		}
		
		int level = Math.max( 0, armor.level() );
		
		if (Random.Int( level + 7 ) >= 6) {
			
			DeferedDamage debuff = defender.buff( DeferedDamage.class );
			if (debuff == null) {
				debuff = new DeferedDamage();
				debuff.attachTo( defender );
			}
			debuff.prolong( damage );

            defender.showStatus( CharSprite.WARNING, StringsManager.getVar(R.string.Viscosity_Status), damage );
			
			return 0;
			
		} else {
			return damage;
		}
	}
	
	@Override
	public String name( String weaponName) {
        return Utils.format(R.string.Viscosity_Txt, weaponName );
	}

	@Override
	public Glowing glowing() {
		return PURPLE;
	}
	
	public static class DeferedDamage extends Buff {
		
		@Packable
		protected int damage = 0;

		@Override
		public boolean attachTo(@NotNull Char target ) {
			if (super.attachTo( target )) {
				postpone( TICK );
				return true;
			} else {
				return false;
			}
		}
		
		public void prolong( int damage ) {
			this.damage += damage;
		}

		@Override
		public int icon() {
			return BuffIndicator.DEFERRED;
		}
		
		@Override
		public String name() {
            return Utils.format(super.name(), damage);
        }

		@Override
		public String desc() {
            return Utils.format(super.desc(), damage);
		}
		
		@Override
		public boolean act() {
			if (target.isAlive()) {
				
				target.damage( 1, this );
				if (target == Dungeon.hero && !target.isAlive()) {
					// FIXME
					Glyph glyph = new Viscosity();
					Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.GLYPH), glyph.name(), Dungeon.depth ) );
                    GLog.n(StringsManager.getVar(R.string.DeferedDamage_Killed_Txt), glyph.name() );
					
					Badges.validateDeathFromGlyph();
				}
				spend( TICK );
				
				if (--damage <= 0) {
					detach();
				}
			} else {
				detach();
			}
			
			return true;
		}
	}
}
