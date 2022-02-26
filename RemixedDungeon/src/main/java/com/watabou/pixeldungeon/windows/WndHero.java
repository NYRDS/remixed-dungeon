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
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.pixeldungeon.windows.WndBuffInfo;
import com.nyrds.platform.input.Touchscreen;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.GuiProperties;
import com.nyrds.util.Util;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.buffs.CharModifier;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.ui.ImageButton;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.elements.LabeledTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

import lombok.val;

public class WndHero extends WndTabbed {
	private static final int WIDTH		= 100;
	private static final int TAB_WIDTH	= 50;
	
	private StatsTab stats;
	private BuffsTab buffs;

	private TextureFilm film;
	
	public WndHero() {
		
		super();

		SmartTexture icons = TextureCache.get(Assets.BUFFS_LARGE);
		film = new TextureFilm(icons, 16, 16 );
		
		stats = new StatsTab();
		add( stats );
		
		buffs = new BuffsTab();
		add( buffs );

        add( new LabeledTab( this, StringsManager.getVar(R.string.WndHero_Stats)) {
			public void select( boolean value ) {
				super.select( value );
				stats.setVisible(stats.active = selected);
			}
		} );
        add( new LabeledTab( this, StringsManager.getVar(R.string.WndHero_Buffs)) {
			public void select( boolean value ) {
				super.select( value );
				buffs.setVisible(buffs.active = selected);
			}
		} );
		for (Tab tab : tabs) {
			tab.setSize( TAB_WIDTH, tabHeight() );
		}
		
		resize( WIDTH, (int)Math.max( stats.height()+ GAP, buffs.height() + GAP ) );
		
		select( 0 );
	}
	
	private class StatsTab extends Group {

		private static final int GAP = 2;
		
		private float pos;
		
		public StatsTab() {
			
			final Hero hero = Dungeon.hero;

			Text title = PixelScene.createText( 
				Utils.format( R.string.WndHero_StaTitle, hero.lvl(), hero.className() ).toUpperCase(), GuiProperties.titleFontSize());
			title.hardlight( TITLE_COLOR );
			add( title );
			
			RedButton btnCatalogus = new RedButton( R.string.WndHero_StaCatalogus ) {
				@Override
				protected void onClick() {
					hide();
					GameScene.show( new WndCatalogus() );
				}
			};
			btnCatalogus.setRect( 0, title.getY() + title.height(), btnCatalogus.reqWidth() + 2, btnCatalogus.reqHeight() + 2 );
			add( btnCatalogus );
			
			RedButton btnJournal = new RedButton( R.string.WndHero_StaJournal) {
				@Override
				protected void onClick() {
					hide();
					GameScene.show( new WndJournal() );
				}
			};
			btnJournal.setRect( 
				btnCatalogus.right() + 1, btnCatalogus.top(), 
				btnJournal.reqWidth() + 2, btnJournal.reqHeight() + 2 );
			add( btnJournal );
			
			pos = btnCatalogus.bottom() + GAP;

            statSlot(StringsManager.getVar(R.string.WndHero_Health), hero.hp() + "/" + hero.ht() );
            statSlot(StringsManager.getVar(R.string.Mana_Title), hero.getSkillPoints() + "/" + hero.getSkillPointsMax() );

			Hunger hunger = hero.hunger();

            statSlot(StringsManager.getVar(R.string.WndHero_Satiety),
					Utils.EMPTY_STRING+((int)((Hunger.STARVING - hunger.getHungerLevel())/ Hunger.STARVING * 100))+"%");

            statSlot(StringsManager.getVar(R.string.WndHero_Stealth), hero.stealth());

            statSlot(StringsManager.getVar(R.string.WndHero_Awareness), Utils.EMPTY_STRING+(int)(hero.getAwareness() * 100)+"%");

            statSlot(StringsManager.getVar(R.string.WndHero_AttackSkill),  hero.attackSkill(CharsList.DUMMY));
            statSlot(StringsManager.getVar(R.string.WndHero_DefenceSkill), hero.defenseSkill(CharsList.DUMMY));


            statSlot(StringsManager.getVar(R.string.WndHero_Exp), hero.getExp() + "/" + hero.maxExp() );

			pos += GAP;
            statSlot(StringsManager.getVar(R.string.WndHero_Str), hero.effectiveSTR() );
            statSlot(StringsManager.getVar(R.string.WndHero_SkillLevel), hero.skillLevel());

            statSlot(StringsManager.getVar(R.string.WndHero_Gold), Statistics.goldCollected );
            statSlot(StringsManager.getVar(R.string.WndHero_Depth), Statistics.deepestFloor );


			pos += GAP;
		}
		
		private void statSlot( String label, String value ) {
			
			Text txt = PixelScene.createText( label, GuiProperties.regularFontSize() );
			txt.setY(pos);
			add( txt );
			
			txt = PixelScene.createText( value, GuiProperties.regularFontSize() );
			txt.setX(PixelScene.align( WIDTH * 0.65f ));
			txt.setY(pos);
			add( txt );
			
			pos += GAP + txt.baseLine();
		}
		
		private void statSlot( String label, int value ) {
			statSlot( label, Integer.toString( value ) );
		}

		public float height() {
			return pos;
		}
	}
	
	private class BuffsTab extends Group {
		
		private static final int GAP = 2;
		
		private float pos;
		
		public BuffsTab() {
			Dungeon.hero.forEachBuff(buff -> buffSlot(buff));
		}
		
		private void buffSlot( CharModifier buff ) {
			
			int index = buff.icon();
			
			if (index != BuffIndicator.NONE) {
				val icon = new ImageButton( new Image(TextureCache.get(buff.textureLarge()),16, index) ) {
					@Override
					protected void onClick() {
						GameScene.show( new WndBuffInfo(buff));
					}
				};
				icon.setPos(GAP - 1, pos);

				Text txt = PixelScene.createText( buff.name(), GuiProperties.regularFontSize() );
				txt.setX(icon.width() + (GAP * 2));
				txt.setY(pos + (int)(icon.height() - txt.baseLine()) / 2);
				val txtTouch = new TouchArea(txt) {
					@Override
					protected void onClick(Touchscreen.Touch touch) {
						GameScene.show( new WndBuffInfo(buff));
					}
				};
				add( icon );
				add(txtTouch);
				add( txt );
				
				pos += GAP + icon.height();
			} else {
				if(Util.isDebug()) {
					Text txt = PixelScene.createText(buff.name(), GuiProperties.regularFontSize());
					txt.setX(GAP);
					txt.setY(pos + (int) (16 - txt.baseLine()) / 2);


					val txtTouch = new TouchArea(txt) {
						@Override
						protected void onClick(Touchscreen.Touch touch) {
							GameScene.show( new WndBuffInfo(buff));
						}
					};
					add(txtTouch);
					add( txt );

					pos += GAP + 16;
				}
			}
		}
		
		public float height() {
			return pos;
		}
	}
}
