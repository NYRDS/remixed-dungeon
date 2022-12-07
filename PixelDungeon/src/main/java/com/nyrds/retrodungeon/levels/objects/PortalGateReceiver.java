package com.nyrds.retrodungeon.levels.objects;

import com.nyrds.retrodungeon.windows.WndPortalReturn;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;

public class PortalGateReceiver extends PortalGate {
	@Override
	public boolean interact(Char chr) {
		if(!(chr instanceof Char)) {
			return false;
		}

		Hero hero = (Hero)chr;

		if(!used && hero.belongings.getItem(Amulet.class) == null && hero.portalLevelPos != null){
			if(!animationRunning){
				if (!activated){
					playStartUpAnim();
				} else {
					GameScene.show(new WndPortalReturn(this, hero, hero.portalLevelPos));
				}
			}
		} else{
			GLog.w( TXT_USED );
		}
		return false;
	}
}
