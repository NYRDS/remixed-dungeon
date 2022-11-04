package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.Position;
import com.nyrds.pixeldungeon.windows.WndPortal;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;

import org.json.JSONException;
import org.json.JSONObject;

public class PortalGateSender extends PortalGate {

	private static final String TARGET = "target";

	@Packable
	protected Position target;

	public boolean portalInteract(Hero hero) {
		if(!used && hero.getBelongings().getItem(Amulet.class) == null){
			if(!animationRunning){
				if (!activated){
					playStartUpAnim();
				} else {
					GameScene.show(new WndPortal(this, hero, target));
				}
			}
		} else{
            GLog.w(StringsManager.getVar(R.string.PortalGate_Used));
		}
		return false;
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
		super.setupFromJson(level, obj);

		if(obj.has(TARGET)){
			JSONObject portalDesc = obj.getJSONObject(TARGET);
			String levelId = portalDesc.optString("levelId" ,"1");
			target = new Position(levelId,
							portalDesc.optInt("x" ,1),
							portalDesc.optInt("y" ,1));
		}
	}
}
