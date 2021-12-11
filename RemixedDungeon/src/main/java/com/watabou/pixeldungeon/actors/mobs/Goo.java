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

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Camera;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Ooze;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.GooSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Goo extends Boss {

	private static final float PUMP_UP_DELAY	= 2.2f;
	
	public Goo() {
		hp(ht(68));
		exp = 9;
		baseDefenseSkill = 12;
		baseAttackSkill = 11;
		dr = 2;
		spriteClass = GooSprite.class;

		loot(Treasury.Category.POTION, 0.8f);
		
		addResistance( ToxicGas.class );

		collect(new SkeletonKey());
	}
	

	@Packable
	private boolean pumpedUp = false;

	@Override
	public int damageRoll() {
		if (pumpedUp) {
			return Random.NormalIntRange( 7, 21 );
		} else {
			return Random.NormalIntRange( 4, 11 );
		}
	}
	
	@Override
	public int attackSkill( Char target ) {
		return pumpedUp ? 26 : 11;
	}

	@Override
	public boolean act() {
		
		if (level().water[getPos()] && hp() < ht()) {
			heal(1,this);
		}
		
		return super.act();
	}
	
	@Override
    public boolean canAttack(@NotNull Char enemy) {
		return pumpedUp ? distance( enemy ) <= 2 : super.canAttack(enemy);
	}
	
	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		if (Random.Int( 3 ) == 0) {
			Buff.affect( enemy, Ooze.class );
			enemy.getSprite().burst( 0x000000, 5 );
		}
		
		if (pumpedUp) {
			Camera.main.shake( 3, 0.2f );
		}
		
		return damage;
	}
	
	@Override
	public void doAttack(Char enemy) {
		if (pumpedUp || Random.Int( 3 ) > 0) {
			super.doAttack( enemy );
		} else {
			
			pumpedUp = true;
			spend( PUMP_UP_DELAY );
			
			((GooSprite)getSprite()).pumpUp();
			
			if (CharUtils.isVisible(this)) {
                getSprite().showStatus( CharSprite.NEGATIVE, StringsManager.getVar(R.string.Goo_StaInfo1));
                GLog.n(StringsManager.getVar(R.string.Goo_Info1));
			}
		}
	}
	
	@Override
	public boolean attack(@NotNull Char enemy ) {
		boolean result = super.attack( enemy );
		pumpedUp = false;
		return result;
	}
	
	@Override
	public boolean getCloser(int target) {
		pumpedUp = false;
		return super.getCloser( target );
	}

	@Override
	public void die(@NotNull NamedEntityKind cause) {
		super.die( cause );

		Badges.validateBossSlain(Badges.Badge.BOSS_SLAIN_1);

        yell(StringsManager.getVar(R.string.Goo_Info2));
	}
	
	@Override
	public void notice() {
		super.notice();
        yell(StringsManager.getVar(R.string.Goo_Info3));
	}

	@Override
	public boolean zap(@NotNull Char enemy) {
		pumpedUp = false;
		return true;
	}
}
