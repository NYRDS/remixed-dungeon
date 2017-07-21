package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.utils.Position;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import org.json.JSONException;
import org.json.JSONObject;

public class PortalGate extends LevelObject {

	protected Position returnTo;

	protected boolean activated = false;
	protected boolean animationRunning = false;
	protected boolean used = false;
	protected boolean infiniteUses = false;

	protected int uses;

	protected static final String TXT_USED = Game.getVar(R.string.PortalGate_Used);
	protected static final String TXT_ACTIVATED = Game.getVar(R.string.PortalGate_Activated);

	public PortalGate(){
		super(-1);
	}

	public PortalGate(int pos) {
		super(pos);
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
		if(obj.has("uses")){
			uses = obj.getInt("uses");
		} else {
			infiniteUses = true;
		}

		if(obj.has("position")){
			JSONObject portalDesc = obj.getJSONObject("position");
			returnTo = new Position(
					portalDesc.optString("levelId" ,"1"),
					portalDesc.optInt("cell" ,1));
		}
	}

	@Override
	public boolean interact(Hero hero) {
		return false;
	}

	public void useUp(){
		if (infiniteUses == false){
			uses = uses - 1;
			if (uses < 1){
				used = true;
			}
		}

	}

	private static final String USED = "used";
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		used = bundle.optBoolean(USED, false);
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(USED, used);
	}

	@Override
	public void burn() {
	}

	@Override
	public boolean stepOn(Char hero) {
		return false;
	}

	@Override
	public String desc() {
		if(activated){
			return Game.getVar(R.string.PortalGate_Desc_Activated);
		}
		return Game.getVar(R.string.PortalGate_Desc);
	}

	@Override
	public String name() {
		return Game.getVar(R.string.PortalGate_Name);
	}

	@Override
	public int image() {
		return 0;
	}

	@Override
	public String texture() {
		return "levelObjects/portals.png";
	}

	@Override
	public int getSpriteXS() {
		return 32;
	}

	@Override
	public int getSpriteYS() {
		return 32;
	}

	protected void playStartUpAnim(){
		animationRunning = true;
		sprite.playAnim(8, false, new Callback() {
			@Override
			public void call() {
				playActiveLoop();
				activated = true;
				animationRunning = false;
				GLog.w( TXT_ACTIVATED );
			}
		}, image() + 0, image() + 1, image() + 2, image() + 3, image() + 4, image() + 5, image() + 6, image() + 7, image() + 8, image() + 9, image() + 10, image() + 11, image() + 12, image() + 13, image() + 14, image() + 15, image() + 16);

	}

	protected void playActiveLoop(){
		sprite.playAnim(8, true, new Callback() {
			@Override
			public void call() {
			}
		}, image() + 17, image() + 18, image() + 19, image() + 20, image() + 21);
	}
}
