/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
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
		isometricShift = true;
	}
	
	@Override
	public void update() {
		super.update();

		setVisible(owner.getVisible());
		alpha(owner.alpha());

		if (getVisible()) {
			x = owner.x + owner.width - owner.visualOffsetX() - width / 2;
			y = owner.y + owner.visualOffsetY() - height;

			final float elapsed = GameLoop.elapsed;
			if (growing) {
				scale.set( scale.x + elapsed * timeScale );
				if (scale.x > maxSize) {
					growing = false;
				}
			} else {
				scale.set( scale.x - elapsed * timeScale );
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
			
			origin.set( width / 2, height / 2 );
			scale.set( Random.Float( 1, maxSize ) );
		}
	}
	
	public static class Alert extends EmoIcon {
		
		public Alert( CharSprite owner ) {
			
			super( owner );
			
			copy( Icons.get( Icons.ALERT ) );
			
			maxSize = 1.3f;
			timeScale = 2;
			
			origin.set( 2.5f, height - 2.5f );
			scale.set( Random.Float( 1, maxSize ) );
		}
	}
	
	public static class Controlled extends EmoIcon {
		
		public Controlled( CharSprite owner ) {
			
			super( owner );
			
			copy( Icons.get( Icons.MIND_CONTROL ) );
			
			maxSize = 1.1f;
			timeScale = 0.5f;
			
			origin.set( -1f, height / 2 );
			scale.set( Random.Float( 1, maxSize ) );
		}
	}

}
