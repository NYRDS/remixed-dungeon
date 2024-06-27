package com.watabou.pixeldungeon.ui;

import com.nyrds.util.GuiProperties;
import com.watabou.noosa.CompositeTextureImage;
import com.watabou.noosa.Text;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.scenes.PixelScene;

/**
 * Created by mike on 15.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */
public abstract class ListItem extends Component implements IClickable {

	protected final CompositeTextureImage   sprite    = new CompositeTextureImage();
	protected final Text    label     = PixelScene.createText(GuiProperties.regularFontSize());
	protected boolean clickable = false;
	protected final int     align     = 24;

	protected ListItem() {
		super();
		add(sprite);
		add(label);
	}

	@Override
	protected void layout() {
		sprite.setY(PixelScene.align(y + (height - sprite.height + sprite.visualOffsetY()) / 2));

		sprite.setX(PixelScene.align(x + sprite.visualOffsetX()));

        label.setX(Math.max(sprite.getX() + sprite.width -sprite.visualOffsetX(), align));
		label.setY(PixelScene.align(y + (height - label.baseLine()) / 2));
	}

	public boolean onClick(float x, float y) {
		if (clickable && inside(x, y)) {
			onClick();
			return true;
		} else {
			return false;
		}
	}

	abstract protected void onClick();
}
