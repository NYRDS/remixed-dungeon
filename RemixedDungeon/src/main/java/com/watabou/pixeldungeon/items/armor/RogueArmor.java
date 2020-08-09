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

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.effects.CellEmitter;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.scenes.CellSelector;
import com.watabou.pixeldungeon.utils.GLog;

import org.jetbrains.annotations.NotNull;

public class RogueArmor extends ClassArmor {
	
	{
		image = 8;
	}
	
	@Override
	public String special() {
		return "RogueArmor_ACSpecial";
	}
	
	@Override
	public void doSpecial(@NotNull Char user) {
		user.selectCell( teleporter );
	}
	
	@Override
	public boolean doEquip(@NotNull Char hero ) {
		if (hero.getHeroClass() == HeroClass.ROGUE) {
			return super.doEquip( hero );
		} else {
			GLog.w( Game.getVar(R.string.RogueArmor_NotRogue) );
			return false;
		}
	}
	
	@Override
	public String desc() {
		return Game.getVar(R.string.RogueArmor_Desc);
	}

	protected static CellSelector.Listener teleporter = new  CellSelector.Listener() {
		
		@Override
		public void onSelect(Integer target, Char selector) {
			if (target != null) {

				if (!Dungeon.level.fieldOfView[target] || 
					!(Dungeon.level.passable[target] || Dungeon.level.avoid[target]) || 
					Actor.findChar( target ) != null) {
					
					GLog.w( Game.getVar(R.string.RogueArmor_Fov) );
					return;
				}
				
				for (Mob mob : Dungeon.level.getCopyOfMobsArray()) {
					if (Dungeon.level.fieldOfView[mob.getPos()] && !(mob instanceof NPC)) {
						Buff.prolong( mob, Blindness.class, 2 );
						mob.setState(MobAi.getStateByClass(Wandering.class));
						mob.getSprite().emitter().burst( Speck.factory( Speck.LIGHT ), 4 );
					}
				}

				WandOfBlink.appear( selector, target );
				CellEmitter.get( target ).burst( Speck.factory( Speck.WOOL ), 10 );
				Sample.INSTANCE.play( Assets.SND_PUFF );
				Dungeon.level.press( target, selector );
				Dungeon.observe();
				
				selector.spendAndNext( Actor.TICK );
			}
		}
		
		@Override
		public String prompt() {
			return Game.getVar(R.string.RogueArmor_Prompt);
		}
	};
}