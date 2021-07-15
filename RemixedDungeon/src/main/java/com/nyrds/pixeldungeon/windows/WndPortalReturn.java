package com.nyrds.pixeldungeon.windows;

import com.nyrds.pixeldungeon.levels.objects.PortalGate;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.Position;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.hero.Hero;

public class WndPortalReturn extends WndPortal {

	@Override
	protected String getDesc(){
        return StringsManager.getVar(R.string.WndPortal_Info_Return);
    }

	public WndPortalReturn(final PortalGate portal, final Hero hero, final Position returnTo ) {
		super(portal, hero, returnTo);
	}
}
