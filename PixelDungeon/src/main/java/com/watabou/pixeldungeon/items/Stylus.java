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
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.scrolls.BlankScroll;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndBag;

import java.util.ArrayList;

public class Stylus extends Item {
	
	private static final String TXT_SELECT_ARMOR	= Game.getVar(R.string.Stylus_SelectArmor);
	private static final String TXT_INSCRIBED		= Game.getVar(R.string.Stylus_Inscribed);
	
	private static final float TIME_TO_INSCRIBE = 2;
	
	private static final String AC_INSCRIBE = Game.getVar(R.string.Stylus_ACInscribe);
	
	{
		image = ItemSpriteSheet.STYLUS;
		
		stackable = true;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_INSCRIBE );
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals(AC_INSCRIBE)) {

			setCurUser(hero);
			GameScene.selectItem( itemSelector, WndBag.Mode.INSCRIBABLE, TXT_SELECT_ARMOR );
			
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
	
	private void inscribeEffect(){
		getCurUser().getSprite().operate( getCurUser().getPos() );
		getCurUser().getSprite().centerEmitter().start( PurpleParticle.BURST, 0.05f, 10 );
		Sample.INSTANCE.play( Assets.SND_BURNING );
		
		getCurUser().spend( TIME_TO_INSCRIBE );
		getCurUser().busy();
	}
	
	private void inscribeArmor ( Armor armor ) {
		
		detach( getCurUser().belongings.backpack );
		
		Class<? extends Armor.Glyph> oldGlyphClass = armor.glyph != null ? armor.glyph.getClass() : null;
		Armor.Glyph glyph = Armor.Glyph.random();
		while (glyph.getClass() == oldGlyphClass) {
			glyph = Armor.Glyph.random();
		}
		
		GLog.w( TXT_INSCRIBED, glyph.name(), armor.name() );
		
		armor.inscribe( glyph );
		
		inscribeEffect();
	}
	
	private void inscribeScroll (BlankScroll scroll){
		
		scroll.detach( getCurUser().belongings.backpack );
		
		inscribeEffect();
		
		Scroll inscribedScroll = Scroll.createRandomScroll();
		getCurUser().collect(inscribedScroll);
		GLog.i(Hero.TXT_YOU_NOW_HAVE, inscribedScroll.name());	// Let know which scroll was inscribed
	}
	
	@Override
	public int price() {
		return 50 * quantity();
	}
	
	private final WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect( Item item ) {
			if (item != null) {
				if(item instanceof Armor){
					inscribeArmor ( (Armor)item );
				}
				if(item instanceof BlankScroll){
					inscribeScroll( (BlankScroll)item );
				}
			}
		}
	};
}
