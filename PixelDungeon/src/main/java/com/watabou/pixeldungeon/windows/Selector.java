package com.watabou.pixeldungeon.windows;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

/**
 * Created by mike on 16.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Selector {

	private int width, height;

	private Window parent;

	private RedButton btnPlus;
	private RedButton btnMinus;
	private RedButton btnDefault;

	private static final String TXT_PLUS      = Game
			.getVar(R.string.WndSettings_ZoomIn);
	private static final String TXT_MINUS     = Game
			.getVar(R.string.WndSettings_ZoomOut);

	public Selector(Window wnd, int width, int height) {
		this.width = width;
		this.height = height;
		parent = wnd;
	}

	public void enable(boolean p, boolean m, boolean d) {
		btnDefault.enable(d);
		btnMinus.enable(m);
		btnPlus.enable(p);
	}

	public void enable(boolean val) {
		enable(val,val,val);
	}

	public void remove() {
		parent.remove(btnPlus);
		parent.remove(btnMinus);
		parent.remove(btnDefault);
	}

	public void setText(String text) {
		btnDefault.text(text);
	}

	public float add(float y,String text, final PlusMinusDefault actions) {
		int w = height;

		btnPlus = new RedButton(TXT_PLUS) {
			@Override
			protected void onClick() {
				actions.onPlus();
			}
		};
		parent.add(btnPlus.setRect(width - w, y, w, height));

		btnMinus = new RedButton(TXT_MINUS) {
			@Override
			protected void onClick() {
				actions.onMinus();
			}
		};
		parent.add(btnMinus.setRect(0, y, w, height));

		btnDefault = new RedButton(text) {
			@Override
			protected void onClick() {
				actions.onDefault();
			}
		};
		btnDefault.setRect(btnMinus.right(), y, width - btnPlus.width()
				- btnMinus.width(), height);
		parent.add(btnDefault);

		return btnMinus.bottom();
	}

	public interface PlusMinusDefault {
		void onPlus();
		void onMinus();
		void onDefault();
	}
}
