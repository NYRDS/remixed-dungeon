package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.scenes.GameScene;

import lombok.SneakyThrows;

/**
 * Created by mike on 30.05.2016.
 */
class MenuButton extends ImageButton {

	private Class<? extends Window> wndClass;
	private boolean enabled = true;

	public MenuButton(Image image, Class<? extends Window> _wndClass) {
		super(image);

		wndClass = _wndClass;

		width += 4;
		height += 4;
	}

	@Override
	protected void layout() {
		super.layout();

		image.x = x + 2;
		image.y = y + 2;
	}

	@Override
	protected void onTouchDown() {
		if (enabled) {
			image.brightness(1.5f);
			Sample.INSTANCE.play(Assets.SND_CLICK);
		}
	}

	public void enable(boolean val) {
		enabled = val;
		if(!enabled) {
			image.brightness(0.5f);
		}
	}

	@Override
	protected void onTouchUp() {
		if (enabled) {
			image.resetColor();
		}
	}

	@Override
	@SneakyThrows
	protected void onClick() {
		if (enabled) {
			GameScene.show(wndClass.newInstance());
		}
	}
}
