
package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.sprites.ImpSprite;
import com.watabou.pixeldungeon.utils.Utils;

public class ImpShopkeeper extends Shopkeeper {

	{
		spriteClass = ImpSprite.class;
	}
	
	private boolean seenBefore = false;
	
	@Override
    public boolean act() {

		if (!seenBefore && CharUtils.isVisible(this)) {
            say( Utils.format(R.string.ImpShopkeeper_Greetings) );
			seenBefore = true;
		}
		
		return super.act();
	}
	
}
