
package com.watabou.pixeldungeon.effects;

import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.PointF;

import org.jetbrains.annotations.NotNull;

public class CellEmitter {

	@NotNull
	public static Emitter get( int cell ) {
		PointF p = DungeonTilemap.tileToWorld( cell );
		Emitter emitter = GameScene.emitter();
		emitter.pos( p.x, p.y, DungeonTilemap.SIZE, DungeonTilemap.SIZE );
		return emitter;
	}
	
	public static Emitter center( int cell ) {
		
		PointF p = DungeonTilemap.tileToWorld( cell );
		
		Emitter emitter = GameScene.emitter();
		emitter.pos( p.x + DungeonTilemap.SIZE / 2, p.y + DungeonTilemap.SIZE / 2 );
		
		return emitter;
	}
	
	public static Emitter bottom( int cell ) {
		
		PointF p = DungeonTilemap.tileToWorld( cell );
		
		Emitter emitter = GameScene.emitter();
		emitter.pos( p.x, p.y + DungeonTilemap.SIZE, DungeonTilemap.SIZE, 0 );
		
		return emitter;
	}
}
