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
package com.watabou.pixeldungeon.ui;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.audio.Sample;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.effects.BadgeBanner;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.windows.WndBadge;

import java.util.ArrayList;

public class BadgesList extends ScrollPane {

	private final ArrayList<ListItem> items = new ArrayList<>();
	
	public BadgesList( boolean global ) {
		super( new Component() );
		
		for (Badges.Badge badge : Badges.filtered( global )) {
			
			if (badge.image == -1) {
				continue;
			}
			
			ListItem item = new ListItem(badge);
			content.add( item );
			items.add( item );
		}
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		float pos = 0;
		
		int size = items.size();
		for (int i=0; i < size; i++) {
			items.get( i ).setRect( 0, pos, width, ListItem.HEIGHT );
			pos += ListItem.HEIGHT;
		}
		
		content.setSize( width, pos );
	}
	
	@Override
	public void onClick( float x, float y ) {
		int size = items.size();
		for (int i=0; i < size; i++) {
			if (items.get( i ).onClick( x, y )) {
				break;
			}
		}
	}

	private static class ListItem extends Component {
		
		private static final float HEIGHT	= 20;
		
		private Badges.Badge badge;
		
		private Image icon;
		private Text label;
		
		public ListItem( Badges.Badge badge ) {
			super();
			
			this.badge = badge;
			icon.copy( BadgeBanner.image( badge.image ));
			label.text( badge.description );
		}
		
		@Override
		protected void createChildren() {
			icon = new Image();
			add( icon );
			
			label = PixelScene.createText(GuiProperties.regularFontSize());
			add( label );
		}
		
		@Override
		protected void layout() {
			icon.setX(x);
			icon.setY(PixelScene.align( y + (height - icon.height) / 2 ));

            label.setX(icon.getX() + icon.width + 2);
			label.setY(PixelScene.align( y + (height - label.baseLine()) / 2 ));
		}
		
		public boolean onClick( float x, float y ) {
			if (inside( x, y )) {
				Sample.INSTANCE.play( Assets.SND_CLICK, 0.7f, 0.7f, 1.2f );
				GameLoop.addToScene( new WndBadge( badge ) );
				return true;
			} else {
				return false;
			}
		}
	}
}
