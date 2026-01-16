

package com.nyrds.platform.gl;

import android.opengl.GLES20;

public class Uniform {

	private final int location;
	
	public Uniform( int location ) {
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
	
	public void value( int value ) {
		GLES20.glUniform1i( location, value );
	}
	
	public void value1f( float value ) {
		GLES20.glUniform1f( location, value );
	}
	
	public void value2f( float v1, float v2 ) {
		GLES20.glUniform2f( location, v1, v2 );
	}
	
	public void value4f( float v1, float v2, float v3, float v4 ) {
		GLES20.glUniform4f( location, v1, v2, v3, v4 );
	}
	
	public void valueM3( float[] value ) {
		GLES20.glUniformMatrix3fv( location, 1, false, value, 0 );
	}
	
	public void valueM4( float[] value ) {
		GLES20.glUniformMatrix4fv( location, 1, false, value, 0 );
	}
}
