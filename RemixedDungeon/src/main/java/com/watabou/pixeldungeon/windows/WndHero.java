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
import com.nyrds.platform.util.StringsManager;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.windows.elements.LabeledTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

public class WndHero extends WndTabbed {
	public static final int WIDTH		= 100;
	private static final int TAB_WIDTH	= 50;
	
	private StatsTab stats;
	private BuffsTab buffs;

	private TextureFilm film;
	
	public WndHero() {
		
		super();

		SmartTexture icons = TextureCache.get(Assets.BUFFS_LARGE);
		film = new TextureFilm(icons, 16, 16 );
		
		stats = new StatsTab(this);
		add( stats );
		
		buffs = new BuffsTab();
		add( buffs );

        add( new LabeledTab( this, StringsManager.getVar(R.string.WndHero_Stats)) {
			public void select( boolean value ) {
				super.select( value );
				stats.setVisible(stats.setActive(selected));
			}
		} );
        add( new LabeledTab( this, StringsManager.getVar(R.string.WndHero_Buffs)) {
			public void select( boolean value ) {
				super.select( value );
				buffs.setVisible(buffs.setActive(selected));
			}
		} );
		for (Tab tab : tabs) {
			tab.setSize( TAB_WIDTH, tabHeight() );
		}
		
		resize( WIDTH, (int)Math.max( stats.height()+ GAP, buffs.height() + GAP ) );
		
		select( 0 );
	}

}
