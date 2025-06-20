package com.nyrds.platform.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.glscripts.Script;
import com.watabou.glwrap.Quad;
import com.watabou.noosa.Camera;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class NoosaScript extends Script {

	public Uniform uCamera;
	public Uniform uModel;
	public Uniform uTex;
	public Uniform uColorM;
	public Uniform uColorA;
	public Attribute aXY;
	public Attribute aUV;
	
	private Camera lastCamera;
	
	public NoosaScript() {
		
		super();
		compile( shader() );
		
		uCamera	= uniform( "uCamera" );
		uModel	= uniform( "uModel" );
		uTex	= uniform( "uTex" );
		uColorM	= uniform( "uColorM" );
		uColorA	= uniform( "uColorA" );
		aXY		= attribute( "aXYZW" );
		aUV		= attribute( "aUV" );
		
	}
	
	@Override
	public void use() {
		
		super.use();
		
		aXY.enable();
		aUV.enable();
		
	}

	@Override
	public void unuse() {
		super.unuse();

		aXY.disable();
		aUV.disable();
	}

	public void drawElements(FloatBuffer vertices, ShortBuffer indices, int size ) {

		vertices.position( 0 );
		aXY.vertexPointer( 2, 4, vertices );
		
		vertices.position( 2 );
		aUV.vertexPointer( 2, 4, vertices );
		
		Gdx.gl20.glDrawElements( GL20.GL_TRIANGLES, Quad.SIZE * size, GL20.GL_UNSIGNED_SHORT, indices );
	}
	
	public void drawQuad( FloatBuffer vertices ) {

		if(vertices.limit()<16){
			throw new AssertionError();
		}

		vertices.position( 0);
		aXY.vertexPointer( 2, 4, vertices );
		
		vertices.position( 2 );
		aUV.vertexPointer( 2, 4, vertices );

		Gdx.gl20.glDrawElements( GL20.GL_TRIANGLES, Quad.SIZE, GL20.GL_UNSIGNED_SHORT, Quad.getIndices( 1 ) );
		
	}
	
	public void drawQuadSet( FloatBuffer vertices, int size ) {

		if (size == 0) {
			return;
		}

		if(vertices.limit() < 16 * size){
			throw new AssertionError();
		}

		drawElements(vertices, Quad.getIndices( size ), size );
	}
	
	public void lighting( float rm, float gm, float bm, float am, float ra, float ga, float ba, float aa ) {
		uColorM.value4f( rm, gm, bm, am );
		uColorA.value4f( ra, ga, ba, aa );
	}
	
	public void resetCamera() {
		lastCamera = null;
	}
	
	public void camera( Camera camera ) {
		if (camera == null) {
			camera = Camera.main;
		}
		if (camera != lastCamera) {
			lastCamera = camera;

			uCamera.valueM4( camera.matrix );
			
			Gdx.gl20.glScissor( 
				camera.x, 
				GameLoop.height- camera.screenHeight - camera.y,
				camera.screenWidth, 
				camera.screenHeight );
		}
	}
	
	public static NoosaScript get() {
		return Script.use( NoosaScript.class );
	}
	
	
	protected String shader() {
		return SHADER;
	}
	
	private static final String SHADER =
		
		"uniform mat4 uCamera;" +
		"uniform mat4 uModel;" +
		"attribute vec4 aXYZW;" +
		"attribute vec2 aUV;" +
		"varying vec2 vUV;" +
		"void main() {" +
		"  gl_Position = uCamera * uModel * aXYZW;" +
		"  vUV = aUV;" +
		"}" +
		
		"//\n" +

		"varying vec2 vUV;" +
		"uniform sampler2D uTex;" +
		"uniform vec4 uColorM;" +
		"uniform vec4 uColorA;" +
		"void main() {" +
		"  gl_FragColor = texture2D( uTex, vUV ) * uColorM + uColorA;" +
		"}";
}
