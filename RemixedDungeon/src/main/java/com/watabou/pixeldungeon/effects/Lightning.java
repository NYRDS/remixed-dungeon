
package com.watabou.pixeldungeon.effects;

import android.opengl.GLES20;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.audio.Sample;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import javax.microedition.khronos.opengles.GL10;

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
			int width = Dungeon.level.getWidth();

			cx[i] = (c % width + 0.5f) * DungeonTilemap.SIZE;
			cy[i] = (c / width + 0.5f) * DungeonTilemap.SIZE + Image.isometricShift();
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
				arcS.setAngle((float)(Math.atan2( dy, dx ) * A));
                arcS.setScaleX ((float)Math.sqrt( dx * dx + dy * dy ) / arcS.width);
				
				dx = ex - x2;
				dy = ey - y2;
				Image arcE = arcsE[i];
				arcE.am = alpha;
				arcE.setAngle((float)(Math.atan2( dy, dx ) * A));
                arcE.setScaleX( (float)Math.sqrt( dx * dx + dy * dy ) / arcE.width);
				arcE.setX(x2 - arcE.origin.x);
				arcE.setY(y2 - arcE.origin.x);
			}
		}
	}
	
	@Override
	public void draw() {
		GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
		super.draw();
		GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
	}
}
