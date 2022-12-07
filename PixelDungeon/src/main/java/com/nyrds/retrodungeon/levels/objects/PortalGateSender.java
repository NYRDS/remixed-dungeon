package com.nyrds.retrodungeon.levels.objects;

import com.nyrds.retrodungeon.utils.Position;
import com.nyrds.retrodungeon.windows.WndPortal;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Amulet;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class PortalGateSender extends PortalGate {

	static final String TARGET = "target";

	protected Position target;

	@Override
	public boolean interact(Char chr) {

		if(!(chr instanceof Char)) {
			return false;
		}

		Hero hero = (Hero)chr;

		if(!used && hero.belongings.getItem(Amulet.class) == null){
			if(!animationRunning){
				if (!activated){
					playStartUpAnim();
				} else {
					GameScene.show(new WndPortal(this, hero, target));
				}
			}
		} else{
			GLog.w( TXT_USED );
		}
		return false;
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
		super.setupFromJson(level, obj);

		if(obj.has("target")){
			JSONObject portalDesc = obj.getJSONObject("target");
			String levelId = portalDesc.optString("levelId" ,"1");
			target = new Position(levelId, portalDesc.optInt("x" ,1), portalDesc.optInt("y" ,1));
		}
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		target = (Position) bundle.get(TARGET);
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(TARGET, target);
	}
}
