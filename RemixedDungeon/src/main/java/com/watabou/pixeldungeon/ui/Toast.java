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

import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.scenes.PixelScene;

import org.jetbrains.annotations.Nullable;

public class Toast extends Component {

	private static final float MARGIN_HOR	= 4;
	private static final float MARGIN_VER	= 4;

	protected NinePatch bg;
	protected SimpleButton close;
	protected Text text;

	@Nullable
	protected Image icon;

	HBox hBox;

	public Toast( String text) {
		this(text, null);
	}

	public Toast( String text, @Nullable Image icon) {
		super();

		hBox = new HBox(Window.STD_WIDTH);

		hBox.setAlign(VBox.Align.Center);
		hBox.setGap((int) MARGIN_HOR);

		bg = Chrome.get( Chrome.Type.TOAST_TR );
		add( bg );

		if(icon!=null) {
			this.icon = icon;
			hBox.add(icon);
		}

		this.text = PixelScene.createText(text, GuiProperties.regularFontSize());
		hBox.add( this.text );

		close = new SimpleButton( Icons.get( Icons.CLOSE ) ) {
			protected void onClick() {
				onClose();
			}
		};

		hBox.add( close );
		add(hBox);

		width = hBox.width() + MARGIN_HOR * 2;
		height = hBox.height() + MARGIN_VER * 2;
	}


	@Override
	protected void layout() {
		super.layout();

		bg.setX(x);
		bg.setY(y);
		bg.size( width, height );
		hBox.setPos(x + MARGIN_HOR, y + MARGIN_VER);
	}

	protected void onClose() {}
}