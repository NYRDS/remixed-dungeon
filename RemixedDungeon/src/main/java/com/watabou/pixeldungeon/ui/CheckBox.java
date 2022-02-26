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

import com.watabou.pixeldungeon.scenes.PixelScene;

public class CheckBox extends RedButton {

	private boolean checked;
	
	public CheckBox( String label ) {
		this(label,false);
	}

	public CheckBox (String label, boolean checked) {
		super(label);
		this.checked = checked;
		icon( Icons.get( checked ? Icons.CHECKED : Icons.UNCHECKED ) );
	}

	@Override
	protected void layout() {
		super.layout();
		
		float margin = (height - text.baseLine()) / 2;
		
		text.setX(PixelScene.align( PixelScene.uiCamera, x + margin ));
		text.setY(PixelScene.align( PixelScene.uiCamera, y + margin ));

        icon.setX(PixelScene.align( PixelScene.uiCamera, x + width - margin - icon.width));
		icon.setY(PixelScene.align( PixelScene.uiCamera, y + (height - icon.height()) / 2 ));
	}
	
	public boolean checked() {
		return checked;
	}
	
	public void checked( boolean value ) {
		if (checked != value) {
			checked = value;
			icon.copy( Icons.get( checked ? Icons.CHECKED : Icons.UNCHECKED ) );
		}
	}
	
	@Override
	protected void onClick() {
		super.onClick();
		checked( !checked );
	}
}
