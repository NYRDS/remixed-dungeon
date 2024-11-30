package com.watabou.noosa;

import com.nyrds.platform.compatibility.RectF;
import com.nyrds.platform.gl.NoosaScript;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.glwrap.Quad;

import java.nio.FloatBuffer;

public class NinePatch extends Visual {

	public final SmartTexture texture;

	protected final float[] vertices;
	protected final FloatBuffer verticesBuffer;

	protected final RectF outterF;
	protected final RectF innerF;

	protected final int marginLeft;
	protected final int marginRight;
	protected final int marginTop;
	protected final int marginBottom;

	protected final float nWidth;
	protected final float nHeight;
	
	public NinePatch( Object tx, int margin ) {
		this( tx, margin, margin, margin, margin );
	}
	
	public NinePatch( Object tx, int left, int top, int right, int bottom ) {
		this( tx, 0, 0, 0, 0, left, top, right, bottom );
	}
	
	public NinePatch( Object tx, int x, int y, int w, int h, int margin ) {
		this( tx, x, y, w, h, margin, margin, margin, margin );
	}
	
	public NinePatch( Object tx, int x, int y, int w, int h, int left, int top, int right, int bottom ) {
		super( 0, 0, 0, 0 );
		
		texture = TextureCache.get( tx );
		w = w == 0 ? texture.width : w;
		h = h == 0 ? texture.height : h;
		
		nWidth = w;
		setWidth(w);
		nHeight = h;
		setHeight(h);
		
		vertices = new float[16];
		verticesBuffer = Quad.createSet( 9 );

		marginLeft	= left;
		marginRight	= right;
		marginTop	= top;
		marginBottom= bottom;
		
		outterF = texture.uvRect( x, y, x + w, y + h );
		innerF = texture.uvRect( x + left, y + top, x + w - right, y + h - bottom );

		updateVertices();
	}
	
	protected void updateVertices() {
		verticesBuffer.position( 0 );

		float right = width - marginRight;
		float bottom = height - marginBottom;
		
		Quad.fill( vertices, 
			0, marginLeft, 0, marginTop, outterF.left, innerF.left, outterF.top, innerF.top );
		verticesBuffer.put( vertices );
		Quad.fill( vertices, 
			marginLeft, right, 0, marginTop, innerF.left, innerF.right, outterF.top, innerF.top );
		verticesBuffer.put( vertices );
		Quad.fill( vertices,
			right, width, 0, marginTop, innerF.right, outterF.right, outterF.top, innerF.top );
		verticesBuffer.put( vertices );
		
		Quad.fill( vertices, 
			0, marginLeft, marginTop, bottom, outterF.left, innerF.left, innerF.top, innerF.bottom );
		verticesBuffer.put( vertices );
		Quad.fill( vertices, 
			marginLeft, right, marginTop, bottom, innerF.left, innerF.right, innerF.top, innerF.bottom );
		verticesBuffer.put( vertices );
		Quad.fill( vertices,
			right, width, marginTop, bottom, innerF.right, outterF.right, innerF.top, innerF.bottom );
		verticesBuffer.put( vertices );

		Quad.fill( vertices,
			0, marginLeft, bottom, height, outterF.left, innerF.left, innerF.bottom, outterF.bottom );
		verticesBuffer.put( vertices );
		Quad.fill( vertices,
			marginLeft, right, bottom, height, innerF.left, innerF.right, innerF.bottom, outterF.bottom );
		verticesBuffer.put( vertices );
		Quad.fill( vertices,
			right, width, bottom, height, innerF.right, outterF.right, innerF.bottom, outterF.bottom );
		verticesBuffer.put( vertices );
	}
	
	public int marginLeft() {
		return marginLeft;
	}
	
	public int marginRight() {
		return marginRight;
	}
	
	public int marginTop() {
		return marginTop;
	}
	
	public int marginBottom() {
		return marginBottom;
	}
	
	public int marginHor() {
		return marginLeft + marginRight;
	}
	
	public int marginVer() {
		return marginTop + marginBottom;
	}
	
	public float innerWidth() {
		return width - marginLeft - marginRight;
	}
	
	public float innerHeight() {
		return height - marginTop - marginBottom;
	}
	
	public float innerRight() {
		return width - marginRight;
	}
	
	public float innerBottom() {
		return height - marginBottom;
	}
	
	public void size( float width, float height ) {
		this.setWidth(width);
		this.setHeight(height);
		updateVertices();
	}
	
	@Override
	public void draw() {
		
		super.draw();

		NoosaScript script = NoosaScript.get();
		
		texture.bind();
		
		script.camera( camera() );
		
		script.uModel.valueM4( matrix );
		script.lighting( 
			rm, gm, bm, am, 
			ra, ga, ba, aa );
		
		script.drawQuadSet( verticesBuffer, 9 );
		
	}
}
