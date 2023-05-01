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
package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;

public class DummySprite extends MobSprite {

	public static DummySprite instance = new DummySprite();

	public DummySprite() {
		super();
		
		texture("hero/empty.png");
		
		TextureFilm frames = new TextureFilm( texture, 1, 1 );

		idle = new Animation( 1, true );
		idle.frames( frames, 0);

		attack = new Animation(1, false);
		attack.frames(frames, 0);

		zap = new Animation(1, false);
		zap.frames(frames, 0);

		run = new Animation( 1, true );
		run.frames( frames, 0);
		
		die = new Animation( 1, false );
		die.frames( frames, 0 );

		play( idle );
	}

	@Override
	public void add(State state) {
	}

	@Override
	public void remove(State state) {
	}

	@Override
	public void removeAllStates() {
	}

	@Override
	public int blood() {
		return 0;
	}

	@Override
	public boolean getVisible() {
		return false;
	}

	@Override
	public boolean isVisible() {
		return false;
	}
}
