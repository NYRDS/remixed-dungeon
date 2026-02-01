

package com.nyrds.platform.gl;

import com.badlogic.gdx.Gdx;

import java.nio.FloatBuffer;

public class Attribute {

	private int location;
	
	public Attribute( int location ) {
		this.location = location;
	}
	
	public int location() {
		return location;
	}
	
	public void enable() {
		Gdx.gl20.glEnableVertexAttribArray( location );
	}
	
	public void disable() {
		Gdx.gl20.glDisableVertexAttribArray( location );
	}
	
	public void vertexPointer( int size, int stride, FloatBuffer ptr ) {
		Gdx.gl20.glVertexAttribPointer( location, size, Gdx.gl20.GL_FLOAT, false, stride * Float.SIZE / 8, ptr );
	}
}
