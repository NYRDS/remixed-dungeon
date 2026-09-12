package com.nyrds.platform.gl;

// headless: no-op
public class Uniform {

	private final int location;

	public Uniform(int location) {
		this.location = location;
	}

	public int location() {
		return location;
	}

	public void enable() {
	}

	public void disable() {
	}

	public void value(int value) {
	}

	public void value1f(float value) {
	}

	public void value2f(float v1, float v2) {
	}

	public void value4f(float v1, float v2, float v3, float v4) {
	}

	public void valueM3(float[] value) {
	}

	public void valueM4(float[] value) {
	}
}
