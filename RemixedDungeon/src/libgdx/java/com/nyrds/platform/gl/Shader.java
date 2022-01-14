/*
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.nyrds.platform.gl;


import com.badlogic.gdx.Gdx;

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
	
	public void source( String src ) {
		Gdx.gl20.glShaderSource( handle, src );
	}
	
	public void compile() {
		Gdx.gl20.glCompileShader( handle );

		IntBuffer status = IntBuffer.allocate(1);
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
		shader.source( src );
		shader.compile();
		return shader;
	}
}
