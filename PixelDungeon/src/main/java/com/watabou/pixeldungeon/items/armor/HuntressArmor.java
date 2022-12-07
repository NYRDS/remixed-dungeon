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
package com.watabou.pixeldungeon.items.armor;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.missiles.Shuriken;
import com.watabou.pixeldungeon.sprites.MissileSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

import java.util.HashMap;

public class HuntressArmor extends ClassArmor {
	
	private static final String TXT_NO_ENEMIES   = Game.getVar(R.string.HuntressArmor_NoEnemies);
	protected static final String TXT_NOT_HUNTRESS = Game.getVar(R.string.HuntressArmor_NotHuntress);
	
	private static final String AC_SPECIAL = Game.getVar(R.string.HuntressArmor_ACSpecial); 
	
	{
		image = 14;
		hasHelmet = true;
		coverHair = true;
	}
	
	private HashMap<Callback, Mob> targets = new HashMap<>();
	
	@Override
	public String special() {
		return AC_SPECIAL;
	}

	@Override
	public void doSpecial() {
		
		Item proto = new Shuriken();
		
		for (Mob mob : Dungeon.level.mobs) {
			if (Dungeon.level.fieldOfView[mob.getPos()]) {
				
				Callback callback = new Callback() {	
					@Override
					public void call() {
						getCurUser().attack( targets.get( this ) );
						targets.remove( this );
						if (targets.isEmpty()) {
							getCurUser().spendAndNext( getCurUser().attackDelay() );
						}
					}
				};
				
				((MissileSprite)getCurUser().getSprite().getParent().recycle( MissileSprite.class )).
					reset( getCurUser().getPos(), mob.getPos(), proto, callback );
				
				targets.put( callback, mob );
			}
		}
		
		if (targets.size() == 0) {
			GLog.w( TXT_NO_ENEMIES );
			return;
		}

		getCurUser().getSprite().zap( getCurUser().getPos() );
		getCurUser().busy();
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		if (hero.heroClass == HeroClass.HUNTRESS) {
			return super.doEquip( hero );
		} else {
			GLog.w( TXT_NOT_HUNTRESS );
			return false;
		}
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.HuntressArmor_Desc);
	}
}