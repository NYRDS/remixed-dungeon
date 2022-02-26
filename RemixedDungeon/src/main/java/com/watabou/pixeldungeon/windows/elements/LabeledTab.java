package com.watabou.pixeldungeon.windows.elements;

import com.nyrds.util.GuiProperties;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.windows.WndTabbed;

public class LabeledTab extends Tab {

	private Text btLabel;

	public LabeledTab(WndTabbed parent, String label) {
		super(parent);
		btLabel.text(label);
	}

	@Override
	protected void createChildren() {
		super.createChildren();

		btLabel = PixelScene.createText(GuiProperties.titleFontSize());
		add(btLabel);
	}

	@Override
	protected void layout() {
		super.layout();

		btLabel.setX(PixelScene.align(x + (width  - btLabel.width()) / 2));
		btLabel.setY(PixelScene.align(y + (height - btLabel.baseLine()) / 2) - 1);
		if (!selected) {
			btLabel.setY(btLabel.getY() - 2);
		}
	}

	@Override
	public void select(boolean value) {
		super.select(value);
		btLabel.am = selected ? 1.0f : 0.6f;
	}
}