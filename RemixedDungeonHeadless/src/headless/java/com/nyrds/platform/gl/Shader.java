package com.nyrds.platform.gl;

// headless: shaders are accepted and discarded
public class Shader {

	public static final int VERTEX = 1;
	public static final int FRAGMENT = 2;

	private final int handle;

	public Shader(int type) {
		handle = 1;
	}

	public int handle() {
		return handle;
	}

	public void compile(String src) {
	}

	public void delete() {
	}

	public static Shader createCompiled(int type, String src) {
		return new Shader(type);
	}
}
