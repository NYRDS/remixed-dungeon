package com.nyrds.platform.gl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

import org.lwjgl.BufferUtils;

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

		IntBuffer status = BufferUtils.createIntBuffer(1);

		Gdx.gl20.glGetProgramiv( handle, GL20.GL_LINK_STATUS, status);
		if (status.get() == GL20.GL_FALSE) {
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
