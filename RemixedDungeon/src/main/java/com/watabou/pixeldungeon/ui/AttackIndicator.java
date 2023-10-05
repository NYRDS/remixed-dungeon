
package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;



public class AttackIndicator extends Tag {
	
	private static final float ENABLED	= 1.0f;
	private static final float DISABLED	= 0.3f;
	
	private static AttackIndicator instance;
	
	private Image sprite = null;

	@Nullable
	static private Char lastTarget = null;

	private final Char hero;

	public AttackIndicator(Char hero) {
		super( DangerIndicator.COLOR );

		this.hero = hero;
		
		instance = this;
		
		setSize( 24, 24 );
		visible( false );
		enable( false );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		if (sprite != null) {
			sprite.setX(x + (width - sprite.width()) / 2);
			sprite.setY(y + (height - sprite.height()) / 2);

			if(sprite.camera()!= null) {
				PixelScene.align(sprite);
			}
		}
	}	
	
	@Override
	public void update() {
		super.update();

		if(lastTarget !=null && !lastTarget.isAlive()) {
			lastTarget = null;
			visible(false);
			return;
		}

		if (Dungeon.hero.isAlive()) {

			if (!Dungeon.hero.isReady()) {
				enable( false );
			}		
			
		} else {
			visible( false );
		}
	}
	
	private void checkEnemies(Hero hero) {
		var candidates = new ArrayList<Char>();

		candidates.clear();
		int v = hero.visibleEnemies();
		for (int i=0; i < v; i++) {
			Char mob = hero.visibleEnemy( i );

			if (hero.canAttack(mob) && !mob.friendly(hero)) {
				candidates.add( mob );
			}
		}
		
		if (!candidates.contains( lastTarget )) {
			if (candidates.isEmpty()) {
				lastTarget = null;
			} else {
				target( Random.element( candidates ));
				flash();
			}
		} else {
			if (!bg.getVisible()) {
				flash();
			}
		}
		
		visible( lastTarget != null );
		enable( bg.getVisible() );
	}
	
	private void updateImage(@NotNull Char target) {
		if (sprite != null) {
			sprite.killAndErase();
		}

		if(target.invalid()) {
			return;
		}

		sprite = target.newSprite().avatar();
		add(sprite);

		sprite.setX(x + (width - sprite.width()) / 2 + 1);
		sprite.setY(y + (height - sprite.height()) / 2);
		PixelScene.align(sprite);
	}
	
	private boolean enabled = true;
	private void enable( boolean value ) {

		enabled = value;
		if (sprite != null) {
			sprite.alpha( value ? ENABLED : DISABLED );
		}
	}
	
	private void visible( boolean value ) {
		if(!value) {
			enable(false);
		}

		bg.setVisible(value);
		if (sprite != null) {
			sprite.setVisible(value);
		}
	}
	
	@Override
	protected void onClick() {
		if (enabled && lastTarget != null) {
			hero.handle( lastTarget.getPos() );
		}
	}
	
	public static void target( @NotNull Char target ) {
		lastTarget = target;
		instance.updateImage(target);
		
		HealthIndicator.instance.target( target );
	}
	
	public static void updateState(Hero hero) {
		if(instance != null){
			instance.checkEnemies(hero);
		}
	}
}
