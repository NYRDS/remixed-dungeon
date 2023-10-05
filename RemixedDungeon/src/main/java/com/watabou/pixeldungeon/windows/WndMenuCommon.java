
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.ui.CheckBox;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;

public abstract class WndMenuCommon extends Window {

	protected static final int WIDTH      = 112;

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
        menuItems.add(new MenuCheckBox(R.string.WndSettings_Music, GamePreferences.music()) {
            @Override
            protected void onClick() {
                super.onClick();
                GamePreferences.music(checked());
            }
        });


        menuItems.add(new MenuCheckBox(R.string.WndSettings_Sound, GamePreferences.soundFx()) {
            @Override
            protected void onClick() {
                super.onClick();
                GamePreferences.soundFx(checked());
                Sample.INSTANCE.play(Assets.SND_CLICK);
            }
        });
    }


    public static class MenuButton extends RedButton {
        protected MenuButton(int id){
            super(id);
            setSize(WIDTH,BUTTON_HEIGHT);
        }

		protected MenuButton(String txt){
			super(txt);
			setSize(WIDTH,BUTTON_HEIGHT);
		}

        protected MenuButton(String txt, Image img){
            super(txt);
            icon(img);
            setSize(WIDTH,BUTTON_HEIGHT);
        }
	}

    public static class MenuCheckBox extends CheckBox{

        public MenuCheckBox(int id, boolean checked) {
            super(StringsManager.getVar(id), checked);
            setSize(WIDTH,BUTTON_HEIGHT);
        }

		public MenuCheckBox(String label, boolean checked) {
			super(label, checked);
			setSize(WIDTH,BUTTON_HEIGHT);
		}
	}
}
