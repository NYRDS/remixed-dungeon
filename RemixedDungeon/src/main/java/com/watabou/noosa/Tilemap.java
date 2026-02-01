

package com.watabou.noosa;

import com.nyrds.platform.compatibility.RectF;
import com.nyrds.platform.gl.NoosaScript;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Quad;
import com.watabou.utils.Rect;

import org.jetbrains.annotations.NotNull;

import java.nio.FloatBuffer;
import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;
public class Tilemap extends Visual {

	@Getter
	@Setter
	private SmartTexture texture;

	@NotNull
	@Getter
	private final TextureFilm tileset;

	protected int[] data;
	protected int   mapWidth;
	protected int   size;

	private final float cellW;
	private final float cellH;

	protected final float[]     vertices;
	protected FloatBuffer quads;
	
	protected final Rect updated;
	
	public Tilemap( Object tx, TextureFilm tileset ) {
		
		super( 0, 0, 0, 0 );
		
		this.setTexture(TextureCache.get( tx ));
		this.tileset = tileset;
		
		RectF r = tileset.get( 0 );
		cellW = tileset.width( r );
		cellH = tileset.height( r );
		
		vertices = new float[16];
		
		updated = new Rect();
	}
	
	public void map( int[] data, int cols ) {
		
		this.data = data;
		
		mapWidth = cols;

		int mapHeight = data.length / cols;
		size = data.length;
		
		setWidth(cellW * mapWidth);
		setHeight(cellH * mapHeight);
		
		quads = Quad.createSet( size );
		
		updated.set( 0, 0, mapWidth, mapHeight);
	}
	
	protected void updateVertices() {
		
		float y1 = cellH * updated.top;
		float y2 = y1 + cellH;

		for (int i=updated.top; i < updated.bottom; i++) {
			
			float x1 = cellW * updated.left;
			float x2 = x1 + cellW;
			
			int pos = i * mapWidth + updated.left;
			quads.position( 16 * pos );
			
			for (int j=updated.left; j < updated.right; j++) {

				RectF uv = tileset.get( data[pos++] );
				if(uv!= null) {
					vertices[0] = x1;
					vertices[1] = y1;

					vertices[2] = uv.left;
					vertices[3] = uv.top;

					vertices[4] = x2;
					vertices[5] = y1;

					vertices[6] = uv.right;
					vertices[7] = uv.top;

					vertices[8] = x2;
					vertices[9] = y2;

					vertices[10] = uv.right;
					vertices[11] = uv.bottom;

					vertices[12] = x1;
					vertices[13] = y2;

					vertices[14] = uv.left;
					vertices[15] = uv.bottom;
				} else {
					Arrays.fill(vertices,0);
				}

				quads.put(vertices);
				x1 = x2;
				x2 += cellW;
				
			}
			
			y1 = y2;
			y2 += cellH;
		}
		
		updated.setEmpty();
	}
	
	@Override
	public void draw() {
		
		super.draw();
		
		NoosaScript script = NoosaScript.get();
		
		getTexture().bind();
		
		script.uModel.valueM4( matrix );
		script.lighting( 
			rm, gm, bm, am, 
			ra, ga, ba, aa );
		
		if (!updated.isEmpty()) {
			updateVertices();
		}
		
		script.camera( camera );
		script.drawQuadSet( quads, size );

	}

	public Rect updateRegion() {
		return updated;
	}
}
