package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.pixeldungeon.windows.WndPortal;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;

public class PortalGateSender extends PortalGate {
	@Override
	public boolean interact(Hero hero) {
		if(!used && hero.belongings.getItem(Amulet.class) == null){
			if(!animationRunning){
				if (!activated){
					playStartUpAnim();
				} else {
					GameScene.show(new WndPortal(this, hero, returnTo));
				}
			}
		} else{
			GLog.w( TXT_USED );
		}
		return false;
	}
}
