package com.nyrds.platform.gl;

// headless: programs are accepted and discarded
public class Program {

	private final int handle;

	public Program() {
		handle = 1;
	}

	public int handle() {
		return handle;
	}

	public void attach(Shader shader) {
	}

	public void link() {
	}

	public Attribute attribute(String name) {
		return new Attribute(0);
	}

	public Uniform uniform(String name) {
		return new Uniform(0);
	}

	public void use() {
	}

	public void delete() {
	}

	public static Program create(Shader... shaders) {
		Program program = new Program();
		for (Shader shader : shaders) {
			program.attach(shader);
		}
		program.link();
		return program;
	}
}
