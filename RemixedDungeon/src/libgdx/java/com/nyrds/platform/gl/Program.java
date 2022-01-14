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

public class Program {

	private final int handle;
	
	public Program() {
		handle = Gdx.gl20.glCreateProgram();
	}
	
	public int handle() {
		return handle;
	}
	
	public void attach( Shader shader ) {
		Gdx.gl20.glAttachShader( handle, shader.handle() );
	}
	
	public void link() {
		Gdx.gl20.glLinkProgram( handle );
		
		IntBuffer status = IntBuffer.allocate(1);
		Gdx.gl20.glGetProgramiv( handle, Gdx.gl20.GL_LINK_STATUS, status);
		if (status.get() == Gdx.gl20.GL_FALSE) {
			throw new Error( Gdx.gl20.glGetProgramInfoLog( handle ) );
		}
	}
	
	public Attribute attribute(String name ) {
		return new Attribute( Gdx.gl20.glGetAttribLocation( handle, name ) );
	}
	
	public Uniform uniform(String name ) {
		return new Uniform( Gdx.gl20.glGetUniformLocation( handle, name ) );
	}
	
	public void use() {
		Gdx.gl20.glUseProgram( handle );
	}
	
	public void delete() {
		Gdx.gl20.glDeleteProgram( handle );
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
