package com.nyrds.pixeldungeon.windows;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.levels.objects.PortalGate;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.Position;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.TextButton;
import com.watabou.pixeldungeon.ui.Window;

public class WndPortalReturn extends WndPortal {

	@Override
	protected String getDesc(){
		return Game.getVar(R.string.WndPortal_Info_Return);
	}

	public WndPortalReturn(final PortalGate portal, final Hero hero, final Position returnTo ) {
		super(portal, hero, returnTo);
	}
}
