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
package com.watabou.pixeldungeon.effects;

import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;

public class BannerSprites {

	public enum  Type {
		PIXEL_DUNGEON,
		BOSS_SLAIN,
		GAME_OVER,
		SELECT_YOUR_HERO
	}
	
	public static Image get( Type type ) {
		Image icon = new Image( Assets.getBanners() );
		switch (type) {
		case PIXEL_DUNGEON:
			icon.frame( icon.texture.uvRect( 0, 0, 128, 98 ) );
			break;
		case BOSS_SLAIN:
			icon.frame( icon.texture.uvRect( 0, 98, 128, 133 ) );
			break;
		case GAME_OVER:
			icon.frame( icon.texture.uvRect( 0, 133, 128, 168 ) );
			break;
		case SELECT_YOUR_HERO:
			icon.frame( icon.texture.uvRect( 0, 168, 128, 189 ) );
			break;
		}
		return icon;
	}
}
