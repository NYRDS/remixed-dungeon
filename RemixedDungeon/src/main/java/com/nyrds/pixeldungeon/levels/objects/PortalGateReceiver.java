package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndPortalReturn;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;

public class PortalGateReceiver extends PortalGate {

	@Override
	public boolean portalInteract(Hero hero) {
		if(!used && hero.getBelongings().getItem(Amulet.class) == null && hero.portalLevelPos != null && !hero.portalLevelPos.equals(getPosition())){
			if(!animationRunning){
				if (!activated){
					playStartUpAnim();
				} else {
					GameScene.show(new WndPortalReturn(this, hero, hero.portalLevelPos));
				}
			}
		} else{
            GLog.w(StringsManager.getVar(R.string.PortalGate_Used));
		}
		return false;
	}
}
