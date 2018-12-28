package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.windows.HBox;
import com.nyrds.pixeldungeon.windows.RedImageButton;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.RedButton;

/**
 * Created by mike on 16.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Selector extends HBox{

	private RedImageButton btnPlus;
	private RedImageButton btnMinus;
	private RedButton btnDefault;

	public Selector(int width, int height, String text, final PlusMinusDefault actions) {
		super(width);
		this.width = width;
		this.height = height;

		createButtons(text, actions);
	}

	private void createButtons(final String text, final PlusMinusDefault actions) {
		float square_xs = height;

		btnMinus = new RedImageButton(Icons.MINUS.get()) {
			@Override
			protected void onClick() {
				actions.onMinus(Selector.this);
			}
		};
		add(btnMinus.setSize(square_xs, height));

		btnDefault = new RedButton(text) {
			@Override
			protected void onClick() {
				actions.onDefault(Selector.this);
			}
		};
		btnDefault.setSize( width - 2*square_xs, height);
		add(btnDefault);
		btnPlus = new RedImageButton(Icons.PLUS.get()) {
			@Override
			protected void onClick() {
				actions.onPlus(Selector.this);
			}
		};
		add(btnPlus.setSize(square_xs, height));

		setAlign(Align.Left);
	}

	public void regen() {
		btnDefault.regenText();
	}

	public void enable(boolean p, boolean m, boolean d) {
		btnDefault.enable(d);
		btnMinus.enable(m);
		btnPlus.enable(p);
	}

	public void enable(boolean val) {
		enable(val,val,val);
	}

	public void setText(String text) {
		btnDefault.text(text);
	}

	public interface PlusMinusDefault {
		void onPlus(Selector selector);
		void onMinus(Selector selector);
		void onDefault(Selector selector);
	}
}
