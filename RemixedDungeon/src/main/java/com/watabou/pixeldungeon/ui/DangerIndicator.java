
package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.scenes.PixelScene;



public class DangerIndicator extends Tag {
	
	public static final int COLOR	= 0xFF4C4C;
	
	private BitmapText number;
	private Image      icon;
	
	private int enemyIndex = 0;
	
	private int lastNumber = -1;
	private final Char hero;

	public DangerIndicator(Char hero) {
		super( 0xFF4C4C );

		this.hero = hero;
		setSize( 24, 16 );
		
		setVisible(false);
	}
	
	@Override
	protected void createChildren() {
		super.createChildren();
		
		number = new BitmapText(PixelScene.font1x);

		add( number );
		
		icon = Icons.SKULL.get();
		add( icon );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		icon.setX(right() - 10);
        icon.setY(y + (height - icon.height) / 2);
		
		placeNumber();
	}
	
	private void placeNumber() {
		number.setX(right() - 11 - number.width());
		number.setY(PixelScene.align( y + (height - number.baseLine()) / 2 ));
	}
	
	@Override
	public void update() {
		
		if (hero.isAlive()) {
			int v =  hero.visibleEnemies();

			if(v>0) {
				Actor.setRealTimeMultiplier(1);
			} else {
				Actor.setRealTimeMultiplier(10);
			}

			if (v != lastNumber) {
				lastNumber = v;
				if (setVisible(lastNumber > 0)) {
					number.text( Integer.toString( lastNumber ) );
					placeNumber();
					
					flash();
				}
			}
		} else {
			setVisible(false);
		}

		super.update();
	}
	
	@Override
	protected void onClick() {
		int enemies = hero.visibleEnemies();
		if(enemies > 0) {
			enemyIndex++;
			enemyIndex %= enemies;
			Char target = Dungeon.hero.visibleEnemy(enemyIndex);

			HealthIndicator.instance.target(target == HealthIndicator.instance.target() ? null : target);

			Camera.main.target = null;
			Camera.main.focusOn(target.getSprite());
		}
	}
}
