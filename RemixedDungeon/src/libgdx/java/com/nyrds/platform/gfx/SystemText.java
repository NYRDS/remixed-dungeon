package com.nyrds.platform.gfx;

import com.watabou.noosa.Text;

public class SystemText extends Text {
	public SystemText(float baseLine){
		super(0,0,0,0);
	}

	public SystemText(final String text, float size, boolean multiline) {
		super(0,0,0,0);
	}

	public static void updateFontScale() {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void kill() {
	}


	public void measure() {
	}


	@Override
	public float baseLine() {
		return height();
	}

	@Override
	public int lines() {
		return 4;
	}

	public static void invalidate() {
	}
}
