package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.items.ItemUtils;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.nyrds.util.Util;
import com.watabou.noosa.Animation;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.utils.GLog;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class PortalGate extends Deco {

	protected boolean animationRunning = false;

	@Packable
	protected boolean activated = false;

	@Packable
	protected boolean used = false;

	@Packable
	protected boolean infiniteUses = false;

	@Packable
	protected int uses;

	private Animation activation;
	private Animation activatedLoop;


	public PortalGate(){
		this(-1);
	}

	public PortalGate(int pos) {
		super(pos);
		layer = 2;
	}


	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {

		objectDesc = "portalGate";

		super.setupFromJson(level, obj);
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
            return StringsManager.getVar(R.string.PortalGate_Desc_Activated);
        }
		return super.desc();
	}


	protected void playStartUpAnim(){
		animationRunning = true;
		sprite.ifPresent(
				sprite -> sprite.playAnim(activation, () -> {
					playActiveLoop();
					activated = true;
					animationRunning = false;
					GLog.w(StringsManager.getVar(R.string.PortalGate_Activated));
		}));
	}

	private void playActiveLoop(){
		sprite.ifPresent(
				sprite -> sprite.playAnim(activatedLoop, Util.nullCallback));
	}

	@Override
	public void resetVisualState() {
		super.resetVisualState();

		if(activation==null) {
			activation = loadAnimation("activation");
			activatedLoop = loadAnimation("activatedLoop");
		}

		if(activated) {
			playActiveLoop();
		}
	}


	@Override
	public void bump(Presser presser) {
		if(presser instanceof Item) {
			ItemUtils.throwItemAway(getPos());
		}
	}

	@Override
	public boolean nonPassable(Char ch) {
		return false;
	}

	public abstract boolean portalInteract(Hero hero);
}
