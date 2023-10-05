
package com.watabou.pixeldungeon.effects;

import com.nyrds.platform.audio.Sample;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.effects.particles.ElmoParticle;

public class Effects {

	public static void burnFX( int pos ) {
		CellEmitter.get( pos ).burst( ElmoParticle.FACTORY, 6 );
		Sample.INSTANCE.play( Assets.SND_BURNING );
	}

	public static void evaporateFX(int pos) {
		CellEmitter.get( pos ).burst( Speck.factory( Speck.STEAM ), 5 );
	}

	public enum  Type {
		RIPPLE,
		LIGHTNING,
		WOUND,
		RAY,
		CHAIN,
		DEATHSTROKE,
		DEVOUR
	}
	
	public static Image get( Type type ) {
		Image icon = new Image( Assets.EFFECTS );
		switch (type) {
		case RIPPLE:
			icon.frame( icon.texture.uvRect( 0, 0, 16, 16 ) );
			break;
		case LIGHTNING:
			icon.frame( icon.texture.uvRect( 16, 0, 32, 8 ) );
			break;
		case WOUND:
			icon.frame( icon.texture.uvRect( 16, 8, 32, 16 ) );
			break;
		case RAY:
			icon.frame( icon.texture.uvRect( 16, 16, 32, 24 ) );
			break;
		case CHAIN:
			icon.frame( icon.texture.uvRect( 16, 24, 32, 32 ) );
			break;
		case DEATHSTROKE:
			icon.frame( icon.texture.uvRect( 0, 16, 16, 24 ) );
			break;
		case DEVOUR:
			icon.frame( icon.texture.uvRect( 0, 24, 16, 32 ) );
			break;
		}
		return icon;
	}
}
