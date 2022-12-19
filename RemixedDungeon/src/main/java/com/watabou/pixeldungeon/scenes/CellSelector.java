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
package com.watabou.pixeldungeon.scenes;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.utils.CharsList;
import com.nyrds.platform.input.Touchscreen.Touch;
import com.watabou.noosa.Image;
import com.watabou.noosa.TouchArea;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.GameMath;
import com.watabou.utils.PointF;

import org.jetbrains.annotations.NotNull;



public class CellSelector extends TouchArea {

	public Listener listener = null;
	public Char selector = CharsList.DUMMY;
	
	public boolean enabled;
	
	private final float dragThreshold;
	
	public CellSelector( DungeonTilemap map ) {
		super( map );
		camera = map.camera();
		dragThreshold = PixelScene.defaultZoom * DungeonTilemap.SIZE / 2;
	}

	public boolean defaultListner() {
		return listener instanceof DefaultCellListener;
	}

	@Override
	protected void onClick( Touch touch ) {
		if (dragging) {
			dragging = false;
		} else {
			select( ((DungeonTilemap)target).screenToTile( 
				(int)touch.current.x, 
				(int)touch.current.y ) );
		}
	}
	
	public void select( int cell ) {
		Hero hero = Dungeon.hero;

		if (!Dungeon.realtime()) {
			enabled = hero.isReady();
		} else {
			enabled = hero.isAlive();
		}

		boolean defaultListener = listener instanceof DefaultCellListener;
		if(hero.myMove() && !defaultListener) {
			enabled = true;
		}

		GLog.debug("CellSelector %b, %s",  enabled, String.valueOf(listener));
		if (enabled && listener != null && cell != Level.INVALID_CELL) {
			GLog.debug("CellSelector %s -> %d", listener.getClass().getSimpleName(), cell);

			var oldListener = listener;

			listener.onSelect( cell, selector);

			if(!defaultListener && oldListener == listener) {
				selector.next();
			}

			GameScene.ready();

		} else {
			GLog.debug("canceled CellSelector %s -> %d", listener.getClass().getSimpleName(), cell);
			GameScene.cancel();
		}
	}
	
	private boolean pinching = false;
	private Touch another;
	private float startZoom;
	private float startSpan;
	
	@Override
	protected void onTouchDown( Touch t ) {

		if (t != touch && another == null) {
					
			if (!touch.down) {
				touch = t;
				onTouchDown( t );
				return;
			}
			
			pinching = true;
			
			another = t;
			startSpan = PointF.distance( touch.current, another.current );
			startZoom = camera.zoom;

			dragging = false;
		}
	}
	
	@Override
	protected void onTouchUp( Touch t ) {
		if (pinching && (t == touch || t == another)) {
			
			pinching = false;
			
			int zoom = Math.round( camera.zoom );
			camera.zoom( zoom );
			GamePreferences.zoom(zoom - PixelScene.defaultZoom);

			dragging = true;
			if (t == touch) {
				touch = another;
			}
			another = null;
			lastPos.set( touch.current );
		}
	}	
	
	private boolean dragging = false;
	private final PointF lastPos = new PointF();
	
	@Override
	protected void onDrag( Touch t ) {
		 
		camera.target = null;

		if (pinching) {

			float curSpan = PointF.distance( touch.current, another.current );
			camera.zoom( GameMath.gate( 
				PixelScene.minZoom, 
				startZoom * curSpan / startSpan, 
				PixelScene.maxZoom ) );

		} else {
		
			if (!dragging && PointF.distance( t.current, t.start ) > dragThreshold) {
				
				dragging = true;
				lastPos.set( t.current );
				
			} else if (dragging) {
				camera.scroll.offset( PointF.diff( lastPos, t.current ).invScale( camera.zoom ) );
				lastPos.set( t.current );	
			}	
		}
		
	}	
	
	public void cancel() {
		
		if (listener != null) {
			listener.onSelect( null, selector);
		}
		
		GameScene.ready();
	}
	
	public interface Listener {
		void onSelect(Integer cell, @NotNull Char selector);
		String prompt();
        Image icon();
    }
}
