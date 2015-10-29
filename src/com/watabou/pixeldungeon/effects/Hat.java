package com.watabou.pixeldungeon.effects;

import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.utils.Random;

public class Hat extends Image {

	protected float maxSize = 2;
	protected float timeScale = 1;
	
	protected boolean growing	= true;
	
	protected CharSprite owner;
	
	public Hat( CharSprite owner ) {
		
		this.owner = owner;
		GameScene.add(this);
				
		origin.set( width / 2, height / 2 );
		scale.set( Random.Float( 1, maxSize ) );
	}
	
	@Override
	public void update() {
		super.update();
		
		if (getVisible()) {
			if (growing) {
				scale.set( scale.x + Game.elapsed * timeScale );
				if (scale.x > maxSize) {
					growing = false;
				}
			} else {
				scale.set( scale.x - Game.elapsed * timeScale );
				if (scale.x < 1) {
					growing = true;
				}
			}
			
			x = owner.x + owner.width - width / 2;
			y = owner.y - height;
		}
	}
	
	public static class Test extends Hat {
		
		public Test( CharSprite owner ) {
			
			super( owner );
			
			copy( new Image("hats/test.png") );
			
			maxSize = 1.2f;
			timeScale = 0.5f;
			
			origin.set( width / 2, height / 2 );
			scale.set( Random.Float( 1, maxSize ) );
		}
	}	

}
