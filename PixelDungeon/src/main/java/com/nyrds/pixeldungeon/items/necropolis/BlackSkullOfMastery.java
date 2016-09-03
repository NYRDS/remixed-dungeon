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
package com.nyrds.pixeldungeon.items.necropolis;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import java.util.ArrayList;

public class BlackSkullOfMastery extends Item {

	public static final float TIME_TO_READ = 10;

	public static final String AC_NECROMANCY           = Game.getVar(R.string.Necromancer_ACSpecial);
	private static final String TXT_WAY_ALREADY_CHOSEN = Game.getVar(R.string.TomeOfMastery_WayAlreadyChosen);

	{
		stackable = false;
		imageFile = "items/artifacts.png";
		identify();
		image = 19;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );		
		actions.add( AC_NECROMANCY );
		
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_NECROMANCY )) {

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
			//TODO: GameScene.show( new WndChooseWay( this, way1, way2 ) );
		} else {
			
			super.execute( hero, action );
			
		}
	}
	
	@Override
	public boolean doPickUp( Hero hero ) {
		Badges.validateMastery();
		return super.doPickUp( hero );
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	public void choose( HeroSubClass way ) {
		
		detach( getCurUser().belongings.backpack );
				
		getCurUser().subClass = way;
		
		getCurUser().getSprite().operate( getCurUser().getPos() );
		Sample.INSTANCE.play( Assets.SND_MASTERY );
		
		SpellSprite.show( getCurUser(), SpellSprite.MASTERY );
		getCurUser().getSprite().emitter().burst( Speck.factory( Speck.MASTERY ), 12 );
		GLog.w(Game.getVar(R.string.TomeOfMastery_Choose), Utils.capitalize( way.title() ) );
		
		getCurUser().checkIfFurious();
		getCurUser().updateLook();
		
		getCurUser().spendAndNext( BlackSkullOfMastery.TIME_TO_READ );
		getCurUser().busy();
	}
}
