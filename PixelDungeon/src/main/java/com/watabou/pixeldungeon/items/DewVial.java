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
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.ShaftParticle;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class DewVial extends Item {

	private static final int MAX_VOLUME	= 10;
	
	private static final String AC_DRINK	= Game.getVar(R.string.DewVial_ACDRINK);
	
	private static final float TIME_TO_DRINK = 1f;
	
	private static final String TXT_VALUE	= "%+dHP";
	private static final String TXT_STATUS	= "%d/%d";
	
	private static final String TXT_AUTO_DRINK	= Game.getVar(R.string.DewVial_AutoDrink);
	private static final String TXT_COLLECTED	= Game.getVar(R.string.DewVial_Collected);
	private static final String TXT_FULL		= Game.getVar(R.string.DewVial_Full);
	private static final String TXT_EMPTY		= Game.getVar(R.string.DewVial_Empty);
	
	{
		imageFile = "items/vials.png";
		image = 0;
		
		defaultAction = AC_DRINK;
	}
	
	private int volume = 0;
	
	private static final String VOLUME	= "volume";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( VOLUME, getVolume() );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		setVolume(bundle.getInt( VOLUME ));
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (getVolume() > 0) {
			actions.add( AC_DRINK );
		}
		return actions;
	}
	
	private static final double NUM = 20;
	private static final double POW = Math.log10( NUM );
	
	@Override
	public void execute( final Hero hero, String action ) {
		if (action.equals( AC_DRINK )) {
			
			if (getVolume() > 0) {

				int value = (int)Math.ceil( Math.pow( getVolume(), POW ) / NUM * hero.ht() );
				int effect = Math.min( hero.ht() - hero.hp(), value );
				if (effect > 0) {
					hero.hp(hero.hp() + effect);
					hero.getSprite().emitter().burst( Speck.factory( Speck.HEALING ), getVolume() > 5 ? 2 : 1 );
					hero.getSprite().showStatus( CharSprite.POSITIVE, TXT_VALUE, effect );
				}
				
				setVolume(0);
				
				hero.spend( TIME_TO_DRINK );
				hero.busy();
				
				Sample.INSTANCE.play( Assets.SND_DRINK );
				hero.getSprite().operate( hero.getPos() );
				
				updateQuickslot();
				
			} else {
				GLog.w( TXT_EMPTY );
			}
			
		} else {
			
			super.execute( hero, action );
			
		}
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	public boolean isFull() {
		return getVolume() >= MAX_VOLUME;
	}
	
	public void collectDew( Dewdrop dew ) {
		
		GLog.i( TXT_COLLECTED );
		setVolume(getVolume() + dew.quantity());
		if (getVolume() >= MAX_VOLUME) {
			setVolume(MAX_VOLUME);
			GLog.p( TXT_FULL );
		}
		
		updateQuickslot();
	}
	
	public void fill() {
		setVolume(MAX_VOLUME);
		updateQuickslot();
	}
	
	public static void autoDrink( Hero hero ) {
		DewVial vial = hero.belongings.getItem( DewVial.class );
		if (vial != null && vial.isFull()) {
			vial.execute( hero );
			hero.getSprite().emitter().start( ShaftParticle.FACTORY, 0.2f, 3 );
			
			GLog.w( TXT_AUTO_DRINK );
		}
	}
	
	private static final Glowing WHITE = new Glowing( 0xFFFFCC );
	
	@Override
	public Glowing glowing() {
		return isFull() ? WHITE : null;
	}
	
	@Override
	public String status() {
		return Utils.format( TXT_STATUS, getVolume(), MAX_VOLUME );
	}
	
	@Override
	public String toString() {
		return super.toString() + " (" + status() +  ")" ;
	}

	private int getVolume() {
		return volume;
	}

	private void setVolume(int volume) {
		this.volume = volume;
		if(volume == 0) {
			image = 0;
		} else if(volume < MAX_VOLUME/2) {
			image = 1;
		} else if(volume < MAX_VOLUME) {
			image = 2;
		} else {
			image = 3;
		}
	}
}
