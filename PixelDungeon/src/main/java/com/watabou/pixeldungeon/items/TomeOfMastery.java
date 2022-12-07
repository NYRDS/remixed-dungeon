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
package com.watabou.pixeldungeon.items;

import com.nyrds.retrodungeon.items.common.MasteryItem;
import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndChooseWay;

import java.util.ArrayList;

public class TomeOfMastery extends MasteryItem {

	private static final String TXT_BLINDED	= Game.getVar(R.string.TomeOfMastery_Blinded);
	public static final String AC_READ                 = Game.getVar(R.string.TomeOfMastery_ACRead);
	private static final String TXT_WAY_ALREADY_CHOSEN = Game.getVar(R.string.TomeOfMastery_WayAlreadyChosen);

	{
		stackable = false;
		image = ItemSpriteSheet.MASTERY;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );		
		actions.add( AC_READ );
		
		return actions;
	}

	@Override
	public boolean doPickUp( Hero hero ) {
		if (Dungeon.hero.heroClass != HeroClass.NECROMANCER)
		{
			Badges.validateMastery();
		}
		return super.doPickUp( hero );
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_READ )) {
			
			if (hero.buff( Blindness.class ) != null) {
				GLog.w( TXT_BLINDED );
				return;
			}

			if(hero.subClass != HeroSubClass.NONE) {
				GLog.w( TXT_WAY_ALREADY_CHOSEN );
				return;
			}

			setCurUser(hero);
			
			HeroSubClass way1 = null;
			HeroSubClass way2 = null;
			switch (hero.heroClass) {
			case WARRIOR:
				way1 = HeroSubClass.GLADIATOR;
				way2 = HeroSubClass.BERSERKER;
				break;
			case MAGE:
				way1 = HeroSubClass.BATTLEMAGE;
				way2 = HeroSubClass.WARLOCK;
				break;
			case ROGUE:
				way1 = HeroSubClass.FREERUNNER;
				way2 = HeroSubClass.ASSASSIN;
				break;
			case HUNTRESS:
				way1 = HeroSubClass.SNIPER;
				way2 = HeroSubClass.WARDEN;
				break;
			case ELF:
				way1 = HeroSubClass.SCOUT;
				way2 = HeroSubClass.SHAMAN;
				break;
			case NECROMANCER:
				GLog.w( TXT_WAY_ALREADY_CHOSEN );
				return;
			}
			GameScene.show( new WndChooseWay( this, way1, way2 ) );
			
		} else {
			
			super.execute( hero, action );
			
		}
	}
}
