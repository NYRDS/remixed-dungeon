
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
