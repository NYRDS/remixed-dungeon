/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.VBox;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.ui.CheckBox;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

public abstract class WndMenuCommon extends Window {

	protected static final int WIDTH      = 112;
	protected static final int BTN_HEIGHT = 18;
    protected final VBox menuItems;


    public WndMenuCommon(){
        menuItems = new VBox();
	    createItems();

        menuItems.setRect(0,0,WIDTH,menuItems.childsHeight());
        add(menuItems);
        resize(WIDTH, (int) menuItems.childsHeight());
    }

    abstract protected void createItems();

    @Override
	public void onBackPressed() {
		hide();
	}

    protected void addSoundControls(VBox menuItems) {
        menuItems.add(new MenuCheckBox(Game
                .getVar(R.string.WndSettings_Music), PixelDungeon.music()) {
            @Override
            protected void onClick() {
                super.onClick();
                PixelDungeon.music(checked());
            }
        });


        menuItems.add(new MenuCheckBox(Game
                .getVar(R.string.WndSettings_Sound),PixelDungeon.soundFx()) {
            @Override
            protected void onClick() {
                super.onClick();
                PixelDungeon.soundFx(checked());
                Sample.INSTANCE.play(Assets.SND_CLICK);
            }
        });
    }


    public class MenuButton extends RedButton {
		protected MenuButton(String txt){
			super(txt);
			setSize(WIDTH,BTN_HEIGHT);
		}
	}

    public class MenuCheckBox extends CheckBox{

		public MenuCheckBox(String label, boolean checked) {
			super(label, checked);
			setSize(WIDTH,BTN_HEIGHT);
		}
	}
}
