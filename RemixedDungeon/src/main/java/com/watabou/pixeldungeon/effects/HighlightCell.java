package com.watabou.pixeldungeon.effects;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.scenes.GameScene;

import java.util.HashMap;
import java.util.Map;

@LuaInterface
public class HighlightCell extends Image {

	private float t;

	static private final Map<Integer, HighlightCell> cells = new HashMap<>();

	static public void add(int pos) {
		cells.put(pos,  new HighlightCell(pos) );
		GameScene.addToMobLayer(cells.get(pos));
	}

	static public void add(int pos, int color) {
		cells.put(pos, new HighlightCell(pos, color) );
		GameScene.addToMobLayer(cells.get(pos));
	}

	static public void remove(int pos) {
		cells.remove(pos);
	}

	static public void removeAll() {
		for (var cell : cells.values()) {
			cell.killAndErase();
		}
		cells.clear();
	}

	public HighlightCell(int pos) {
		this(pos, 0xFF55AAFF);
	}

	public HighlightCell(int pos, int color ) {
		super( TextureCache.createSolid( color ) );

		setOrigin( 0.5f );
		
		point( DungeonTilemap.tileToWorld( pos ).offset( 
			DungeonTilemap.SIZE / 2, 
			DungeonTilemap.SIZE / 2 ) );
	}
	
	@Override
	public void update() {
		t += GameLoop.elapsed * 2;
		float a = (float) (Math.sin( t ) * 0.4f + 0.3f);
		alpha( a );
		setScale( DungeonTilemap.SIZE * a );
	}
}
