
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.windows.elements.LabeledTab;
import com.watabou.pixeldungeon.windows.elements.Tab;

public class WndChar extends WndTabbed {
	public static final int WIDTH		= 100;
	private static final int TAB_WIDTH	= 50;
	
	private StatsTab stats;
	private BuffsTab buffs;

	private TextureFilm film;
	
	public WndChar(final Char chr) {
		
		super();

		SmartTexture icons = TextureCache.get(Assets.BUFFS_LARGE);
		film = new TextureFilm(icons, 16, 16 );
		
		stats = new StatsTab(chr);
		add( stats );
		
		buffs = new BuffsTab(chr);
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
