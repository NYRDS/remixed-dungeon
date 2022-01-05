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

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Gold extends Item {

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
	public ArrayList<String> actions(Char hero ) {
		return new ArrayList<>();
	}

	@Override
	public boolean doPickUp(@NotNull Char hero ) {
		collect(hero);

		Statistics.goldCollected += quantity();
		Badges.validateGoldCollected();

		GameScene.pickUp( this);
		hero.showStatus( CharSprite.NEUTRAL, TXT_VALUE, quantity() );
		hero.spend( TIME_TO_PICK_UP );
		
		Sample.INSTANCE.play( Assets.SND_GOLD, 1, 1, Random.Float( 0.9f, 1.1f ) );
		
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
            return StringsManager.getVar(R.string.Gold_Collect);
            case 1:
                return StringsManager.getVar(R.string.Gold_Info1) +" "+ StringsManager.getVar(R.string.Gold_Collect);
		default:
            return Utils.format( StringsManager.getVar(R.string.Gold_Info) +" "+ StringsManager.getVar(R.string.Gold_Collect), quantity() );
		}
	}

	@Override
	public int image() {
		int value = quantity();

		if(value > 9999) {
			return  4;
		}

		if(value > 999) {
			return  3;
		}

		if(value > 99) {
			return  2;
		}

		if(value > 9 ) {
			return  1;
		}

		return 0;
	}

	@Override
	public Item random() {
		quantity(Math.max(Random.Int( 20 + Dungeon.depth * 10, 40 + Dungeon.depth * 20 ),0));
		return this;
	}
}
