package com.watabou.pixeldungeon.windows.elements;

import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;

public class Tool extends Button {

	private static final int BGCOLOR = 0x7B8073;

	private Image base;

	public Tool(int x, int y, int width, int height) {
		super();

		base.frame(x, y, width, height);

		this.width = width;
		this.height = height;
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		base = new Image(Assets.getToolbar());
		add(base);
	}

	@Override
	protected void layout() {
		super.layout();

		base.x = x;
		base.y = y;
	}

	@Override
	protected void onTouchDown() {
		base.brightness(1.4f);
	}

	@Override
	protected void onTouchUp() {
		if (active) {
			base.resetColor();
		} else {
			base.tint(BGCOLOR, 0.7f);
		}
	}

	public void enable(boolean value) {
		if (value != active) {
			if (value) {
				base.resetColor();
			} else {
				base.tint(BGCOLOR, 0.7f);
			}
			active = value;
		}
	}
}