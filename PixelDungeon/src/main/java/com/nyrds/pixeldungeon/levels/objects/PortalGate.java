package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mike on 01.07.2016.
 */
public class PortalGate extends LevelObject {

	public PortalGate(){
		super(-1);
	}

	public PortalGate(int pos) {
		super(pos);
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
	}

	@Override
	public boolean interact(Hero hero) {

		sprite.playAnim(6, false, new Callback() {
			@Override
			public void call() {
				sprite.playAnim(6, true, new Callback() {
					@Override
					public void call() {


					}
				}, image() + 17,image() + 18,image() + 19);


			}
		}, image() + 0, image() + 1, image() + 2, image() + 3, image() + 4, image() + 5, image() + 6, image() + 7, image() + 8, image() + 9, image() + 10, image() + 11, image() + 12, image() + 13, image() + 14, image() + 15, image() + 16);

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
