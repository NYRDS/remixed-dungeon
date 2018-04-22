package com.watabou.pixeldungeon.windows.elements;

import android.graphics.RectF;

import com.nyrds.android.util.TrackedRuntimeException;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.ui.Button;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.utils.Utils;

public class Tool extends Button {

	private static final int BGCOLOR = 0x7B8073;

	static private final int SIZE = 16;
	private Image base;

	public Tool(String baseImageFile,int x, int y, int width, int height) {
		super();
		base = new Image(baseImageFile);
		base.frame(x, y, width, height);

		this.width = width;
		this.height = height;
		add(base);
	}

	public Tool(String baseImageFile,int index) {
		super();

		TextureFilm film = new TextureFilm(baseImageFile, SIZE, SIZE);
		base = new Image(baseImageFile);

		RectF frame = film.get(index);
		if (frame==null) {
			throw new TrackedRuntimeException(Utils.format("bad index %d for image %s", index, baseImageFile));
		}
		base.frame(frame);

		height=frame.top - frame.bottom;
		width =frame.left - frame.right;
		add(base);
	}

	public Tool(int x, int y, int width, int height) {
		this(Assets.getToolbar(),x,y,width,height);
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