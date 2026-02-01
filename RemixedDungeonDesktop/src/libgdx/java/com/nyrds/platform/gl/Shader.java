

package com.nyrds.platform.gl;


import com.badlogic.gdx.Gdx;

import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;

public class Shader {

	public static final int VERTEX		= Gdx.gl20.GL_VERTEX_SHADER;
	public static final int FRAGMENT	= Gdx.gl20.GL_FRAGMENT_SHADER;
	
	private final int handle;
	
	public Shader( int type ) {
		handle = Gdx.gl20.glCreateShader( type );
		if(handle==0){
			throw new AssertionError();
		}
	}
	
	public int handle() {
		return handle;
	}


	public void compile(String src) {
		Gdx.gl20.glShaderSource( handle, src );
		Gdx.gl20.glCompileShader( handle );

		IntBuffer status = BufferUtils.createIntBuffer(1);

		Gdx.gl20.glGetShaderiv( handle, Gdx.gl20.GL_COMPILE_STATUS, status);
		if (status.get() == Gdx.gl20.GL_FALSE) {
			throw new Error( Gdx.gl20.glGetShaderInfoLog( handle ) );
		}

	}
	
	public void delete() {
		Gdx.gl20.glDeleteShader( handle );
	}
	
	public static Shader createCompiled( int type, String src ) {
		Shader shader = new Shader( type );
		shader.compile(src);
		return shader;
	}
}
