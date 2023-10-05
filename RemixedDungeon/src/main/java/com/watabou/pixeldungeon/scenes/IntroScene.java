
package com.watabou.pixeldungeon.scenes;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.windows.WndStory;

public class IntroScene extends PixelScene {

	@Override
	public void create() {
		super.create();

        add( new WndStory(StringsManager.getVar(R.string.IntroScene_Txt)) {
			@Override
			public void hide() {
				super.hide();
				InterlevelScene.Do(InterlevelScene.Mode.DESCEND);
			}
		} );
		
		fadeIn();
	}
}
