
package com.watabou.pixeldungeon.effects;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.utils.Random;

public class EmoIcon extends Image {

	protected float maxSize = 2;
	protected float timeScale = 1;

	private boolean growing = true;

	protected CharSprite owner;

	private EmoIcon(CharSprite owner) {
		this.owner = owner;
		setVisible(false);
		GameScene.add( this );
		setIsometricShift(true);
	}
	
	@Override
	public void update() {
		super.update();

		setVisible(owner.getVisible());
		alpha(owner.alpha());

		if (getVisible()) {
            setX(owner.getX() + owner.width - owner.visualOffsetX() - width / 2);
			setY(owner.getY() + owner.visualOffsetY() - height);

			final float elapsed = GameLoop.elapsed;
			if (growing) {
				setScale( scale.x + elapsed * timeScale );
				if (scale.x > maxSize) {
					growing = false;
				}
			} else {
				setScale( scale.x - elapsed * timeScale );
				if (scale.x < 1) {
					growing = true;
				}
			}
		}
	}
	
	public static class Sleep extends EmoIcon {
		
		public Sleep( CharSprite owner ) {
			
			super( owner );
			
			copy( Icons.get( Icons.SLEEP ) );
			
			maxSize = 1.2f;
			timeScale = 0.5f;

			setOrigin( width / 2, height / 2 );
			setScale( Random.Float( 1, maxSize ) );
		}
	}
	
	public static class Alert extends EmoIcon {
		
		public Alert( CharSprite owner ) {
			
			super( owner );
			
			copy( Icons.get( Icons.ALERT ) );
			
			maxSize = 1.3f;
			timeScale = 2;

			setOrigin( 2.5f, height - 2.5f );
			setScale( Random.Float( 1, maxSize ) );
		}
	}
	
	public static class Controlled extends EmoIcon {
		
		public Controlled( CharSprite owner ) {
			
			super( owner );
			
			copy( Icons.get( Icons.MIND_CONTROL ) );
			
			maxSize = 1.1f;
			timeScale = 0.5f;

			setOrigin( -1f, height / 2 );
			setScale( Random.Float( 1, maxSize ) );
		}
	}

}
