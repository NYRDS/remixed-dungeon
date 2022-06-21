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

import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

import static com.watabou.pixeldungeon.ui.Window.GAP;

public class IconTitle extends Component {

    private Image imIcon;
    private Text  tfLabel;
	
	public IconTitle() {
		super();
	}
	
	public IconTitle( Item item ) {
		this(
			new ItemSprite( item ), 
			Utils.capitalize( item.toString() ) );
	}
	
	public IconTitle( Image icon, String label ) {
		super();
		
		icon( icon );
		label( label );
	}
	
	@Override
	protected void createChildren() {
		imIcon = new Image();
		add( imIcon );
		
		tfLabel = PixelScene.createMultiline( GuiProperties.titleFontSize() );
		tfLabel.hardlight( Window.TITLE_COLOR );
		add( tfLabel );
	}
	
	@Override
	protected void layout() {
		imIcon.setX(x - imIcon.visualOffsetX());
		float yShift = imIcon.height() - imIcon.visualHeight();
		imIcon.setY(y - yShift);
		
		tfLabel.setX(PixelScene.align( PixelScene.uiCamera, imIcon.getX() + imIcon.visualOffsetX() + imIcon.visualWidth() + GAP ));
		tfLabel.maxWidth((int)(width - tfLabel.getX()));

		tfLabel.setY(PixelScene.align( PixelScene.uiCamera, imIcon.visualHeight() - tfLabel.baseLine()) );
				
		height = Math.max( imIcon.visualHeight() + GAP, tfLabel.getY() + tfLabel.height() );
	}
	
	public void icon( Image icon ) {
		remove( imIcon );
		icon.setIsometricShift(false);
		add( imIcon = icon );
	}
	
	public void label( String label ) {
		tfLabel.text( label );
	}
	
	public void label( String label, int color ) {
		tfLabel.text( label );
		tfLabel.hardlight( color );
	}
	
	public void color( int color ) {
		tfLabel.hardlight( color );
	}
}
