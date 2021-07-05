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

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.game.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.missiles.Shuriken;
import com.watabou.pixeldungeon.sprites.MissileSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Callback;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class HuntressArmor extends ClassArmor {
	
	{
		image = 14;
		hasHelmet = true;
		coverHair = true;
	}
	
	private final HashMap<Callback, Mob> targets = new HashMap<>();
	
	@Override
	public String special() {
		return "HuntressArmor_ACSpecial";
	}

	@Override
	public void doSpecial(@NotNull Char user) {

		Item proto = new Shuriken();
		
		for (Mob mob : user.level().getCopyOfMobsArray()) {
			if (user.level().fieldOfView[mob.getPos()]) {
				
				Callback callback = new Callback() {	
					@Override
					public void call() {
						user.attack( targets.get( this ) );
						targets.remove( this );
						if (targets.isEmpty()) {
							user.spendAndNext( user.attackDelay() );
						}
					}
				};
				
				((MissileSprite) user.getSprite().getParent().recycle( MissileSprite.class )).
					reset( user.getPos(), mob.getPos(), proto, callback );
				
				targets.put( callback, mob );
			}
		}
		
		if (targets.size() == 0) {
			GLog.w( Game.getVar(R.string.HuntressArmor_NoEnemies) );
			return;
		}

		user.getSprite().zap( user.getPos() );
		user.busy();
	}
	
	@Override
	public boolean doEquip(@NotNull Char hero ) {
		if (hero.getHeroClass() == HeroClass.HUNTRESS) {
			return super.doEquip( hero );
		} else {
			GLog.w( Game.getVar(R.string.HuntressArmor_NotHuntress) );
			return false;
		}
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.HuntressArmor_Desc);
	}
}