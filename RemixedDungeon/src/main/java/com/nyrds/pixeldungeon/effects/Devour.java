package com.nyrds.pixeldungeon.effects;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Effects;

public class Devour extends Image {

	private static final float TIME_TO_FADE = 0.8f;

	private float time;

	public Devour() {
		super( Effects.get( Effects.Type.DEVOUR ) );
		origin.set( width / 2, height / 2 );
	}
	
	public void reset( int p ) {
		revive();
		
		x = (Dungeon.level.cellX(p)) * DungeonTilemap.SIZE + (DungeonTilemap.SIZE - width) / 2;
		y = (Dungeon.level.cellY(p)) * DungeonTilemap.SIZE + (DungeonTilemap.SIZE - height) / 2;
		
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
			scale.x = 1 + p;
		}
	}
	
	public static void hit( Char ch ) {
		hit( ch, 0 );
	}
	
	public static void hit( Char ch, float angle ) {
		Devour w = (Devour)ch.getSprite().getParent().recycle( Devour.class );
		ch.getSprite().getParent().bringToFront( w );
		w.reset( ch.getPos() );
		w.angle = angle;
	}
}
