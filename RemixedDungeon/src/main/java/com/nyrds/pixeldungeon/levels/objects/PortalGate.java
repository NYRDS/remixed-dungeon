package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.Packable;
import com.nyrds.android.util.Util;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class PortalGate extends LevelObject {

	protected boolean animationRunning = false;

	@Packable
	protected boolean activated = false;

	@Packable
	protected boolean used = false;

	@Packable
	protected boolean infiniteUses = false;

	@Packable
	protected int uses;

	{
		textureFile = "levelObjects/portals.png";
	}

	public PortalGate(){
		this(-1);
	}

	public PortalGate(int pos) {
		super(pos);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
	}


	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
		if(obj.has("uses")){
			uses = obj.getInt("uses");
		} else {
			infiniteUses = true;
		}
	}

	@Override
	public boolean interact(Char hero) {
		if(hero instanceof Hero) {
			return portalInteract((Hero)hero);
		}
		return false;
	}

	public void useUp(){
		if (!infiniteUses){
			uses = uses - 1;
			if (uses < 1){
				used = true;
			}
		}

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
	public int getSpriteXS() {
		return 32;
	}

	@Override
	public int getSpriteYS() {
		return 32;
	}

	protected void playStartUpAnim(){
		animationRunning = true;
		sprite.playAnim(8, false, () -> {
			playActiveLoop();
			activated = true;
			animationRunning = false;
			GLog.w( Game.getVar(R.string.PortalGate_Activated) );
		}, image() + 0, image() + 1, image() + 2, image() + 3, image() + 4, image() + 5, image() + 6, image() + 7, image() + 8, image() + 9, image() + 10, image() + 11, image() + 12, image() + 13, image() + 14, image() + 15, image() + 16);

	}

	protected void playActiveLoop(){
		sprite.playAnim(8, true, Util.nullCallback, image() + 17, image() + 18, image() + 19, image() + 20, image() + 21);
	}

	@Override
	public void resetVisualState() {
		if(activated) {
			playActiveLoop();
		}
	}

	public abstract boolean portalInteract(Hero hero);
}
