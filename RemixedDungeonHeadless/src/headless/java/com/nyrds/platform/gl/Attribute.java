package com.nyrds.platform.gl;

import java.nio.FloatBuffer;

// headless: no-op
public class Attribute {

	private final int location;

	public Attribute(int location) {
		this.location = location;
	}

	public int location() {
		return location;
	}

	public void enable() {
	}

	public void disable() {
	}

	public void vertexPointer(int size, int stride, FloatBuffer ptr) {
	}
}
