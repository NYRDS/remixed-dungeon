
package com.watabou.pixeldungeon.effects;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.DungeonTilemap;

public class CheckedCell extends Image {
	
	private float alpha;
	
	public CheckedCell( int pos ) {
		super( TextureCache.createSolid( 0xFF55AAFF ) );

		setOrigin( 0.5f );
		
		point( DungeonTilemap.tileToWorld( pos ).offset( 
			DungeonTilemap.SIZE / 2, 
			DungeonTilemap.SIZE / 2 ) );
		
		alpha = 0.8f;
	}
	
	@Override
	public void update() {
		if ((alpha -= GameLoop.elapsed) > 0) {
			alpha( alpha );
			setScale( DungeonTilemap.SIZE * alpha );
		} else {
			killAndErase();
		}
	}
}
