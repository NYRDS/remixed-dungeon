
package com.watabou.pixeldungeon.ui;

import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.CharSprite;

public class HealthIndicator extends Component {

	private static final float HEIGHT	= 2;
	
	public static HealthIndicator instance;
	
	private Char target;

	private Image bg;
	private Image level;

	public HealthIndicator() {
		super();
		
		instance = this;
	}
	
	@Override
	protected void createChildren() {
		bg = new Image( TextureCache.createSolid( 0xFFcc0000 ) );
		bg.setScaleY(HEIGHT);
		add( bg );

		level = new Image( TextureCache.createSolid( 0xFF00cc00 ) );
		level.setScaleY(HEIGHT);
		add( level );
	}
	
	@Override
	public void update() {
		super.update();
		
		if (target != null && target.isAlive() && target.getSprite().getVisible()) {
			CharSprite sprite = target.getSprite();
			bg.setScaleX (sprite.visualWidth());
			level.setScaleX( sprite.visualWidth() * target.hp() / target.ht());
			float x = sprite.getX() + sprite.visualOffsetX();
			bg.setX(x);
			level.setX(x);
			float y = sprite.getY() + sprite.visualOffsetY() - HEIGHT - 1;
			bg.setY(y);
			level.setY(y);
			
			setVisible(true);
		} else {
			setVisible(false);
		}
	}
	
	public void target( Char ch ) {
		if (ch != null && ch.isAlive()) {
			target = ch;
		} else {
			target = null;
		}
	}
	
	public Char target() {
		return target;
	}
}
