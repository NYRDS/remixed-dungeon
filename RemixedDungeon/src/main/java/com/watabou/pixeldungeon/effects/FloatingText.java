
package com.watabou.pixeldungeon.effects;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.BitmapText;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;

import java.util.ArrayList;
import org.apache.commons.collections4.map.HashedMap;
import java.util.Map;

public class FloatingText extends BitmapText {

	private static final float LIFESPAN	= 1f;
	private static final float DISTANCE	= DungeonTilemap.SIZE;

	private float timeLeft;
	
	private int key = -1;
	
	private static final Map<Integer,ArrayList<FloatingText>> stacks = new HashedMap<>();
	
	public FloatingText() {
		super(PixelScene.chooseFont( 9 ));
		setScale( PixelScene.scale );
		
		speed.y = - DISTANCE / LIFESPAN;
	}
	
	@Override
	public void update() {
		super.update();
		
		if (timeLeft > 0) {
			if ((timeLeft -= GameLoop.elapsed) <= 0) {
				kill();
			} else {
				float p = timeLeft / LIFESPAN;
				alpha( p > 0.5f ? 1 : p * 2 );
			}
		}
	}
	
	@Override
	public void kill() {
		if (key != -1) {
			stacks.get( key ).remove( this );
			key = -1;
		}
		super.kill();
	}
	
	@Override
	public void destroy() {
		kill();
		super.destroy();
	}
	
	public void reset( float x, float y, String text, int color ) {
		
		revive();

		text( text );
		hardlight( color );

		this.setX(PixelScene.align( x - width() / 2 ));
		this.setY(y - height());
		
		timeLeft = LIFESPAN;
	}
	
	/* STATIC METHODS */
	
	public static void show( float x, float y, String text, int color ) {
		((FloatingText) GameScene.status()).reset( x,  y,  text, color );
	}
	
	public static void show( float x, float y, int key, String text, int color ) {
		FloatingText txt = (FloatingText) GameScene.status();
		txt.reset( x,  y,  text, color );
		push( txt, key );
	}
	
	private static void push( FloatingText txt, int key ) {
		
		txt.key = key;
		
		ArrayList<FloatingText> stack = stacks.get( key );
		if (stack == null) {
			stack = new ArrayList<>();
			stacks.put( key, stack );
		}
		
		if (!stack.isEmpty()) {
			FloatingText below = txt;
			int aboveIndex = stack.size() - 1;
			while (aboveIndex >= 0) {
				FloatingText above = stack.get( aboveIndex );
				if (above.getY() + above.height() > below.getY()) {
					above.setY(below.getY() - above.height());
					
					below = above;
					aboveIndex--;
				} else {
					break;
				}
			}
		}
		
		stack.add( txt );
	}
}
