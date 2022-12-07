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

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Gold extends Item {

	private static final String TXT_COLLECT	= Game.getVar(R.string.Gold_Collect);
	private static final String TXT_INFO	= Game.getVar(R.string.Gold_Info)+" "+TXT_COLLECT;
	private static final String TXT_INFO_1	= Game.getVar(R.string.Gold_Info1)+" "+TXT_COLLECT;
	private static final String TXT_VALUE	= "%+d";
	
	public Gold() {
		this( 1 );
	}
	
	public Gold( int value ) {
		this.quantity(value);
		
		imageFile = "items/gold.png";
		stackable = true;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		return new ArrayList<>();
	}
	
	@Override
	public boolean doPickUp( Hero hero ) {
		collect(hero);

		Statistics.goldCollected += quantity();
		Badges.validateGoldCollected();

		GameScene.pickUp( this );
		hero.getSprite().showStatus( CharSprite.NEUTRAL, TXT_VALUE, quantity() );
		hero.spendAndNext( TIME_TO_PICK_UP );
		
		Sample.INSTANCE.play( Assets.SND_GOLD, 1, 1, Random.Float( 0.9f, 1.1f ) );
		
		return true;
	}

	@Override
	public boolean collect(Hero hero) {
		Dungeon.gold(Dungeon.gold() + quantity());

		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public String info() {
		switch (quantity()) {
		case 0:
			return TXT_COLLECT;
		case 1:
			return TXT_INFO_1;
		default:
			return Utils.format( TXT_INFO, quantity() );
		}
	}
	
	@Override
	public void quantity(int value) {
		super.quantity(value);
		
		image = 0;
		
		if(value > 9 ) {
			image = 1;
		}
		
		if(value > 99) {
			image = 2;
		}
		
		if(value > 999) {
			image = 3;
		}
		
		if(value > 9999) {
			image = 4;
		}
		
		
	}
	
	@Override
	public Item random() {
		quantity(Random.Int( 20 + Dungeon.depth * 10, 40 + Dungeon.depth * 20 ));
		return this;
	}
	
	private static final String VALUE	= "value";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( VALUE, quantity() );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		quantity(bundle.getInt( VALUE ));
	}
}
