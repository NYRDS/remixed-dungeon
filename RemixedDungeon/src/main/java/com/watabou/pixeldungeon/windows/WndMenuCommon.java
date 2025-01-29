
package com.watabou.pixeldungeon.windows;

import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.VBox;
import com.nyrds.platform.audio.MusicManager;
import com.nyrds.platform.audio.Sample;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.ui.CheckBox;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Slider;
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
        super.onBackPressed();
    }

    protected void addSoundControls(VBox menuItems) {

        Slider fps = new Slider(R.string.WndSettings_FpsLimit, "30", "120", 0, 2) {
            @Override
            protected void onChange() {
                int value = getSelectedValue();
                GamePreferences.fps_limit(value);
                Game.updateFpsLimit();
            }
        };
        fps.setSelectedValue(GamePreferences.fps_limit());
        fps.setSize(WIDTH,BUTTON_HEIGHT);
        menuItems.add(fps);

        Slider sfx = new Slider(R.string.WndSettings_Sound, "0", "1", 0, 10) {
            @Override
            protected void onChange() {
                int value = getSelectedValue();
                GamePreferences.soundFxVolume(value);
                GamePreferences.soundFx(value > 0);
                Sample.INSTANCE.play(Assets.SND_CLICK);
            }
        };
        sfx.setSelectedValue(GamePreferences.soundFxVolume());
        sfx.setSize(WIDTH,BUTTON_HEIGHT);
        menuItems.add(sfx);

        Slider music = new Slider(R.string.WndSettings_Music, "0", "1", 0, 10) {
            @Override
            protected void onChange() {
                int value = getSelectedValue();
                GamePreferences.musicVolume(value);
                GamePreferences.music(value > 0);
                MusicManager.INSTANCE.resume();
            }
        };
        music.setSelectedValue(GamePreferences.musicVolume());
        music.setSize(WIDTH,BUTTON_HEIGHT);
        menuItems.add(music);
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
