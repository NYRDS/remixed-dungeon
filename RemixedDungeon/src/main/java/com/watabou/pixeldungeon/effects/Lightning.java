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
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.gl.Gl;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Lightning extends Group {

	private static final float DURATION = 0.3f;
	
	private float life;
	
	private int length;
	private float[] cx;
	private float[] cy;
	
	private Image[] arcsS;
	private Image[] arcsE;
	
	private Callback callback;

	public Lightning (int from, int to, Callback callback) {
		int[] cells = new int[2];
		cells[0] = from;
		cells[1] = to;
		init(cells,callback);
	}

	public Lightning( int[] cells) {
		init(cells, null);
	}

	public Lightning( int[] cells, Callback callback ) {
		init(cells, callback);
	}

	private void init ( int[] cells, Callback callback ) {
		this.callback = callback;
		
		Image proto = Effects.get( Effects.Type.LIGHTNING );
		float ox = 0;
		float oy = proto.height / 2;
		
		this.length = cells.length;
		cx = new float[length];
		cy = new float[length];
		
		for (int i=0; i < length; i++) {
			int c = cells[i];
			cx[i] = (c % Dungeon.level.getWidth() + 0.5f) * DungeonTilemap.SIZE;
			cy[i] = (c / Dungeon.level.getWidth() + 0.5f) * DungeonTilemap.SIZE;
		}
		
		arcsS = new Image[length - 1];
		arcsE = new Image[length - 1];
		for (int i=0; i < length - 1; i++) {
			
			Image arc = arcsS[i] = new Image( proto );

			arc.setX(cx[i] - arc.origin.x);
			arc.setY(cy[i] - arc.origin.y);
			arc.setOrigin( ox, oy );
			add( arc );
			
			arc = arcsE[i] = new Image( proto );
			arc.setOrigin( ox, oy );
			add( arc );
		}
		
		life = DURATION;
		
		Sample.INSTANCE.play( Assets.SND_LIGHTNING );
	}
	
	private static final double A = 180 / Math.PI;
	
	@Override
	public void update() {
		super.update();
		
		if ((life -= GameLoop.elapsed) < 0) {
			
			killAndErase();
			if (callback != null) {
				callback.call();
			}
			
		} else {
			
			float alpha = life / DURATION;
			
			for (int i=0; i < length - 1; i++) {
				
				float sx = cx[i];
				float sy = cy[i];
				float ex = cx[i+1];
				float ey = cy[i+1];
				
				float x2 = (sx + ex) / 2 + Random.Float( -4, +4 );
				float y2 = (sy + ey) / 2 + Random.Float( -4, +4 );
				
				float dx = x2 - sx;
				float dy = y2 - sy;
				Image arcS = arcsS[i];
				arcS.am = alpha;
				arcS.angle = (float)(Math.atan2( dy, dx ) * A);
                arcS.setScaleX ((float)Math.sqrt( dx * dx + dy * dy ) / arcS.width);
				
				dx = ex - x2;
				dy = ey - y2;
				Image arcE = arcsE[i];
				arcE.am = alpha;
				arcE.angle = (float)(Math.atan2( dy, dx ) * A);
                arcE.setScaleX( (float)Math.sqrt( dx * dx + dy * dy ) / arcE.width);
				arcE.setX(x2 - arcE.origin.x);
				arcE.setY(y2 - arcE.origin.x);
			}
		}
	}
	
	@Override
	public void draw() {
		Gl.blendSrcAlphaOne();
		super.draw();
		Gl.blendSrcAlphaOneMinusAlpha();
	}
}
