

package com.nyrds.platform.gl;

import android.opengl.GLES20;

public class Shader {

	public static final int VERTEX		= GLES20.GL_VERTEX_SHADER;
	public static final int FRAGMENT	= GLES20.GL_FRAGMENT_SHADER;
	
	private final int handle;
	
	public Shader( int type ) {
		handle = GLES20.glCreateShader( type );
		if(handle==0){
			throw new AssertionError();
		}
	}
	
	public int handle() {
		return handle;
	}
	
	public void source( String src ) {
		GLES20.glShaderSource( handle, src );
	}
	
	public void compile() {
		GLES20.glCompileShader( handle );

		int[] status = new int[1];
		GLES20.glGetShaderiv( handle, GLES20.GL_COMPILE_STATUS, status, 0 );
		if (status[0] == GLES20.GL_FALSE) {
			throw new Error( GLES20.glGetShaderInfoLog( handle ) );
		}
	}
	
	public void delete() {
		GLES20.glDeleteShader( handle );
	}
	
	public static Shader createCompiled( int type, String src ) {
		Shader shader = new Shader( type );
		shader.source( src );
		shader.compile();
		return shader;
	}
}
