package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.windows.WndPortal;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.ui.GameLog;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mike on 01.07.2016.
 */
public class PortalGate extends LevelObject {

	private boolean activated = false;
	private boolean animationRunning = false;
	public boolean used = false;

	private static final String TXT_USED = Game.getVar(R.string.PortalGate_Used);
	private static final String TXT_ACTIVATED = Game.getVar(R.string.PortalGate_Activated);

	public PortalGate(){
		super(-1);
	}

	public PortalGate(int pos) {
		super(pos);
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
	}


	private void playStartUpAnim(){
		animationRunning = true;
		sprite.playAnim(6, false, new Callback() {
			@Override
			public void call() {
				playActiveLoop();
				activated = true;
				animationRunning = false;
				GLog.w( TXT_ACTIVATED );
			}
		}, image() + 0, image() + 1, image() + 2, image() + 3, image() + 4, image() + 5, image() + 6, image() + 7, image() + 8, image() + 9, image() + 10, image() + 11, image() + 12, image() + 13, image() + 14, image() + 15, image() + 16);

	}

	private void playActiveLoop(){
		sprite.playAnim(6, true, new Callback() {
			@Override
			public void call() {
			}
		}, image() + 17, image() + 18, image() + 19, image() + 20, image() + 21);
	}

	@Override
	public boolean interact(Hero hero) {
		if(!used){
			if(!animationRunning){
				if (!activated ){
					playStartUpAnim();
				} else {
					GameScene.show(new WndPortal(this));

				}
			}
		} else{
			GLog.w( TXT_USED );
		}
		return false;
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
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
		return Game.getVar(R.string.LibraryBook_Description);
	}

	@Override
	public String name() {
		return Game.getVar(R.string.LibraryBook_Name);
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
}
