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

import lombok.var;

public class AttackIndicator extends Tag {
	
	private static final float ENABLED	= 1.0f;
	private static final float DISABLED	= 0.3f;
	
	private static AttackIndicator instance;
	
	private Image sprite = null;

	@Nullable
	private static Char lastTarget = null;

	public AttackIndicator() {
		super( DangerIndicator.COLOR );
		
		instance = this;
		
		setSize( 24, 24 );
		visible( false );
		enable( false );
	}
	
	@Override
	protected void layout() {
		super.layout();
		
		if (sprite != null) {
			sprite.x = x + (width - sprite.width()) / 2;
			sprite.y = y + (height - sprite.height()) / 2;

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

		sprite.x = x + (width - sprite.width()) / 2 + 1;
		sprite.y = y + (height - sprite.height()) / 2;
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
			Dungeon.hero.handle( lastTarget.getPos() );
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
