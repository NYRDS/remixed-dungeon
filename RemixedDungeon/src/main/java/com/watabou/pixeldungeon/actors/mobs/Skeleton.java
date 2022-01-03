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
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.levels.NecroBossLevel;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.necropolis.UndeadMob;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class Skeleton extends UndeadMob {

	public Skeleton() {
		hp(ht(25));
		baseDefenseSkill = 9;
		baseAttackSkill  = 12;
		dmgMin = 3;
		dmgMax = 8;
		dr = 5;
		
		exp = 5;
		maxLvl = 10;

		loot(getLoot(), 0.2f);
	}

	@Override
	public Object getLoot() {
		if (!(level() instanceof NecroBossLevel)) {
			return Treasury.getLevelTreasury().worstOf(Treasury.Category.WEAPON,3 );
		}
		return null;
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		
		super.die( cause );
		
		boolean heroKilled = false;
		for (int i=0; i < Level.NEIGHBOURS4.length; i++) {
			Char ch = findChar( getPos() + Level.NEIGHBOURS4[i] );
			if (ch != null && ch.isAlive()) {
				int damage = Math.max( 0,  damageRoll() - ch.defenceRoll(this) / 2 );
				ch.damage( damage, this );
				if (ch == Dungeon.hero && !ch.isAlive()) {
					heroKilled = true;
				}
			}
		}
		
		if (CharUtils.isVisible(this)) {
			Sample.INSTANCE.play( Assets.SND_BONES );
		}
		
		if (heroKilled) {
			Dungeon.fail( Utils.format( ResultDescriptions.getDescription(ResultDescriptions.Reason.MOB), Utils.indefinite( getName() ), Dungeon.depth ) );
            GLog.n(StringsManager.getVar(R.string.Skeleton_Killed));
		}
	}
}
