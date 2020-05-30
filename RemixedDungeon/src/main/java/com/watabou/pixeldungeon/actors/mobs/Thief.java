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
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.ThiefFleeing;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.rings.RingOfHaggler;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Thief extends Mob {

	@Packable(defaultValue = "DUMMY_ITEM")
	public Item item = CharsList.DUMMY_ITEM;
	
	{
		hp(ht(20));
		defenseSkill = 12;
		
		exp = 5;
		maxLvl = 10;
		
		loot = RingOfHaggler.class;
		lootChance = 0.01f;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 7 );
	}
	
	@Override
	protected float _attackDelay() {
		return 0.5f;
	}
	
	@Override
	public void die(NamedEntityKind cause) {
		super.die( cause );
		item.doDrop(this);
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 12;
	}
	
	@Override
	public int dr() {
		return 3;
	}
	
	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		if (item == CharsList.DUMMY_ITEM && enemy instanceof Hero && steal( (Hero)enemy )) {
			setState(MobAi.getStateByClass(ThiefFleeing.class));
		}
		
		return damage;
	}
	
	@Override
	public int defenseProc(Char enemy, int damage) {
		if (getState() instanceof ThiefFleeing) {
			new Gold().doDrop(this);
		}
		
		return damage;
	}
	
	protected boolean steal( Hero hero ) {
		
		Item item = hero.getBelongings().randomUnequipped();
		if (item != null) {
			
			GLog.w( Game.getVar(R.string.Thief_Stole), this.getName(), item.name() );
			
			item.detachAll( hero.getBelongings().backpack );
			this.item = item;
			
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String description() {
		String desc = Game.getVar(R.string.Thief_Desc);
		
		if (item != CharsList.DUMMY_ITEM) {
			desc += Utils.format( Game.getVar(R.string.Thief_Carries), Utils.capitalize( this.getName() ), item.name() );
		}
		
		return desc;
	}
}
