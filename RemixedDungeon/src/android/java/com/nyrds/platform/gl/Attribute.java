

package com.nyrds.platform.gl;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

public class Attribute {

	private final int location;
	
	public Attribute( int location ) {
		this.location = location;
	}
	
	public int location() {
		return location;
	}
	
	public void enable() {
		GLES20.glEnableVertexAttribArray( location );
	}
	
	public void disable() {
		GLES20.glDisableVertexAttribArray( location );
	}
	
	public void vertexPointer( int size, int stride, FloatBuffer ptr ) {
		GLES20.glVertexAttribPointer( location, size, GLES20.GL_FLOAT, false, stride * Float.SIZE / 8, ptr );
	}
}
