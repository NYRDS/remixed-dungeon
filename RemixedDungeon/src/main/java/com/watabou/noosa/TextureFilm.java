

package com.watabou.noosa;

import com.nyrds.platform.compatibility.RectF;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class TextureFilm {

	private final int texWidth;
	private final int texHeight;
	
	protected final HashMap<Integer,RectF> frames = new HashMap<>();
	
	public TextureFilm( Object tx ) {
		
		SmartTexture texture = TextureCache.get( tx );
		
		texWidth = texture.getWidth();
		texHeight = texture.getHeight();
	}

	public TextureFilm( Object tx, int width, int height ) {

		SmartTexture texture = TextureCache.get( tx );

		texWidth = texture.getWidth();
		texHeight = texture.getHeight();
		
		float uw = (float)width / texWidth;
		float vh = (float)height / texHeight;
		int cols = texWidth / width;
		int rows = texHeight / height;
		
		for (int i=0; i < rows; i++) {
			for (int j=0; j < cols; j++) {
				RectF rect = new RectF( j * uw, i * vh, (j+1) * uw, (i+1) * vh );
				add( i * cols + j, rect );
			}
		}
	}

	public int size() {
		return frames.size();
	}

	public void add( int id, RectF rect ) {
		frames.put( id, rect );
	}

	@Nullable
	public RectF get( int id ) {
		return frames.get( id );
	}
	
	public float width( RectF frame ) {
		return frame.width() * texWidth;
	}
	
	public float height( RectF frame ) {
		return frame.height() * texHeight;
	}
}