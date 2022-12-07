package com.nyrds.retrodungeon.levels.objects;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mike on 01.07.2016.
 */
public class ConcreteBlock extends LevelObject {

	int requiredStr = 10;

	public ConcreteBlock() {
		this(-1);
	}

	public ConcreteBlock(int pos) {
		super(pos);
		imageIndex = 8;
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
		requiredStr = obj.optInt("str", 10);
	}

	@Override
	public boolean stepOn(Char hero) {
		return false;
	}

	@Override
	public boolean pushable(Char hero) {
		return true;
	}

	@Override
	public boolean push(Char hero) {
		if(hero instanceof Hero) {
			return ((Hero)hero).effectiveSTR() >= requiredStr && super.push(hero);
		}
		return super.push(hero);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		requiredStr = bundle.getInt("str");
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("str",requiredStr);
	}

	@Override
	public String desc() {
		return String.format(Game.getVar(R.string.ConcreteBlock_Description), requiredStr);
	}

	@Override
	public String name() {
		return Game.getVar(R.string.ConcreteBlock_Name);
	}

	@Override
	public boolean affectLevelObjects() {
		return true;
	}

	@Override
	public boolean nonPassable() {
		return true;
	}
}
