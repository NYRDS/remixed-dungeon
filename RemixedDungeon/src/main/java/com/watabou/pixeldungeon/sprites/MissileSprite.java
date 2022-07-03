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

import com.nyrds.pixeldungeon.effects.ZapEffect;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

public class MissileSprite extends ItemSprite implements Tweener.Listener {

	private Callback callback;
	
	public MissileSprite() {
		super();
		originToCenter();
	}

	public void reset(int from, int to, Item item, Callback listener) {
		revive();

		float scale = item.heapScale();
		setScaleXY(scale, scale);
		view(item);

		this.callback = listener;

		point( DungeonTilemap.tileToWorld( from ) );

		PointF dest = DungeonTilemap.tileToWorld( to );

		PointF d = PointF.diff( dest, point() ); 
		speed.set( d ).normalize().scale( ZapEffect.SPEED );

		if (item.isFliesStraight()) {
			angularSpeed = 0;
			setAngle(135 - (float)(Math.atan2( d.x, d.y ) / Math.PI * 180));
		} else {
			angularSpeed = item.isFliesFastRotating() ? 1440 : 720;
		}

		PosTweener tweener = new PosTweener( this, dest, d.length() / ZapEffect.SPEED );
		tweener.listener = this;
		getParent().add( tweener );
	}

	@Override
	public void onComplete( Tweener tweener ) {
		kill();
		if (callback != null) {
			callback.call();
		}
	}
}
