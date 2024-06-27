
package com.watabou.pixeldungeon.effects;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.GameScene;

public class Wound extends Image {

	private static final float TIME_TO_FADE = 0.8f;
	
	private float time;
	
	public Wound() {
		super( Effects.get( Effects.Type.WOUND ) );
		setOrigin( width / 2, height / 2 );
	}
	
	public void reset( int p ) {
		revive();

        setX((p % Dungeon.level.getWidth()) * DungeonTilemap.SIZE + (DungeonTilemap.SIZE - width) / 2);
		setY((p / Dungeon.level.getWidth()) * DungeonTilemap.SIZE + (DungeonTilemap.SIZE - height) / 2);
		
		time = TIME_TO_FADE;
	}
	
	@Override
	public void update() {
		super.update();
		
		if ((time -= GameLoop.elapsed) <= 0) {
			kill();
		} else {
			float p = time / TIME_TO_FADE;
			alpha( p );
			setScaleX( 1 + p );
		}
	}

	@LuaInterface
	public static void hit( Char ch ) {
		hit( ch, 0 );
	}

	@LuaInterface
	public static void hit( Char ch, float angle ) {
		Group layer = GameScene.getMobsLayer();
		if (layer==null) {
			return;
		}
		Wound w = (Wound)layer.recycle( Wound.class );
		layer.bringToFront( w );
		w.reset( ch.getPos() );
		w.setAngle(angle);
	}

	@LuaInterface
	public static void hit( int pos ) {
		hit( pos, 0 );
	}

	@LuaInterface
	public static void hit( int pos, float angle ) {
		Group layer = GameScene.getMobsLayer();
		if (layer==null) {
			return;
		}

		Wound w = (Wound)layer.recycle( Wound.class );
		layer.bringToFront( w );
		w.reset( pos );
		w.setAngle(angle);
	}
}
