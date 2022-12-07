package com.nyrds.retrodungeon.windows;

import com.nyrds.retrodungeon.levels.objects.PortalGate;
import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.utils.Position;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.hero.Hero;

public class WndPortalReturn extends WndPortal {

	@Override
	protected String getDesc(){
		return Game.getVar(R.string.WndPortal_Info_Return);
	}

	public WndPortalReturn(final PortalGate portal, final Hero hero, final Position returnTo ) {
		super(portal, hero, returnTo);
	}
}
