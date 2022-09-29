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

import com.nyrds.platform.input.Touchscreen.Touch;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.TouchArea;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

public class ScrollPane extends Component {

	protected TouchController controller;
	protected Component content;

	public ScrollPane( Component content ) {
		super();

		this.content = content;
        sendToBack(content);

        width = content.width();
		height = content.height();
		
		content.camera = new Camera( 0, 0, 1, 1, PixelScene.defaultZoom );
		Camera.add( content.camera );
	}

	public void dontCatchTouch() {
		controller.dontCatchTouch();
	}

	@Override
	public void destroy() {
		super.destroy();
		Camera.remove( content.camera );
	}
	
	public void scrollTo( float x, float y ) {
		content.camera.scroll.set( x, y );
	}
	
	@Override
	protected void createChildren() {
		controller = new TouchController();
		add( controller );
	}
	
	@Override
	protected void layout() {
		
		content.setPos( 0, 0 );
		controller.setX(x);
		controller.setY(y);
		controller.setWidth(width);
		controller.setHeight(height);
		
		Point p = camera().cameraToScreen( x, y );
		Camera cs = content.camera;
		cs.x = p.x;
		cs.y = p.y;
		cs.resize( (int)width, (int)height );
	}
	
	public Component content() {
		return content;
	}
	
	public void onClick( float x, float y ) {		
	}
	
	public class TouchController extends TouchArea {
		
		private final float dragThreshold;
		
		public TouchController() {
			super( 0, 0, 0, 0 );
			dragThreshold = PixelScene.defaultZoom * 8;
		}
		
		@Override
		protected void onClick( Touch touch ) {
			if (dragging) {
				dragging = false;
			} else {
				PointF p = content.camera.screenToCamera( (int)touch.current.x, (int)touch.current.y );
				ScrollPane.this.onClick( p.x, p.y );
			}
		}	
		
		// true if dragging is in progress
		private boolean dragging = false;
		// last touch coords
		private final PointF lastPos = new PointF();
		
		@Override
		protected void onDrag( Touch t ) {		
			if (dragging) {
				
				Camera c = content.camera;

				PointF scrollStep = PointF.diff(lastPos, t.current).invScale(c.zoom);
				c.scroll.offset(scrollStep);

				float width = content.width();
				if (c.scroll.x + this.width > width) {
                    c.scroll.x = width - this.width;
				}
				if (c.scroll.x < 0) {
					c.scroll.x = 0;
				}

				float height = content.height();
				if (c.scroll.y + this.height > height) {
					c.scroll.y = height - this.height;
				}
				if (c.scroll.y < 0) {
					c.scroll.y = 0;
				}

				//GLog.debug("scroll this: %3.0f content: %3.0f", this.width, width );


				lastPos.set( t.current );	
				
			} else if (PointF.distance( t.current, t.start ) > dragThreshold) {
				
				dragging = true;
				lastPos.set( t.current );
			}
		}	
	}
}
