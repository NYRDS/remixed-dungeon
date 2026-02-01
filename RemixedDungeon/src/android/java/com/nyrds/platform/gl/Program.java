

package com.nyrds.platform.gl;

import android.opengl.GLES20;

public class Program {

	private final int handle;
	
	public Program() {
		handle = GLES20.glCreateProgram();
	}
	
	public int handle() {
		return handle;
	}
	
	public void attach( Shader shader ) {
		GLES20.glAttachShader( handle, shader.handle() );
	}
	
	public void link() {
		GLES20.glLinkProgram( handle );
		
		int[] status = new int[1];
		GLES20.glGetProgramiv( handle, GLES20.GL_LINK_STATUS, status, 0 );
		if (status[0] == GLES20.GL_FALSE) {
			throw new Error( GLES20.glGetProgramInfoLog( handle ) );
		}
	}
	
	public Attribute attribute(String name ) {
		return new Attribute( GLES20.glGetAttribLocation( handle, name ) );
	}
	
	public Uniform uniform(String name ) {
		return new Uniform( GLES20.glGetUniformLocation( handle, name ) );
	}
	
	public void use() {
		GLES20.glUseProgram( handle );
	}
	
	public void delete() {
		GLES20.glDeleteProgram( handle );
	}
	
	public static Program create( Shader ...shaders ) {
		Program program = new Program();
        for (Shader shader : shaders) {
            program.attach(shader);
        }
		program.link();
		return program;
	}
}
