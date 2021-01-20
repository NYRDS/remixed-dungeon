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
package com.watabou.pixeldungeon.items.food;

import com.nyrds.pixeldungeon.mechanics.CommonActions;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Burning;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.buffs.Roots;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class MysteryMeat extends Food {

	{
		image   = ItemSpriteSheet.MEAT;
		energy  = Hunger.STARVING - Hunger.HUNGRY;
		message = Game.getVar(R.string.MysteryMeat_Message);
	}
	
	@Override
	public void _execute(@NotNull Char chr, @NotNull String action ) {
		
		super._execute(chr, action );
		
		if (action.equals( CommonActions.AC_EAT )) {
			
			switch (Random.Int( 5 )) {
			case 0:
				GLog.w(Game.getVar(R.string.MysteryMeat_Info1));
				Buff.affect(chr, Burning.class ).reignite(chr);
				break;
			case 1:
				GLog.w(Game.getVar(R.string.MysteryMeat_Info2));
				Buff.prolong(chr, Roots.class, Stun.duration(chr) );
				break;
			case 2:
				GLog.w(Game.getVar(R.string.MysteryMeat_Info3));
				Buff.affect(chr, Poison.class,Poison.durationFactor(chr) * chr.ht() / 5 );
				break;
			case 3:
				GLog.w(Game.getVar(R.string.MysteryMeat_Info4));
				Buff.prolong(chr, Slow.class, Slow.duration(chr) );
				break;
			}
		}
	}
	
	public int price() {
		return 5 * quantity();
	}

	@Override
	public Item burn(int cell){
		return morphTo(ChargrilledMeat.class);
	}
	
	@Override
	public Item freeze(int cell){
		return morphTo(FrozenCarpaccio.class);
	}
	
	@Override
	public Item poison(int cell){
		return morphTo(RottenMeat.class);
	}
}
