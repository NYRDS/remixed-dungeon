package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.pixeldungeon.levels.objects.sprites.LevelObjectSprite;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class LevelObject implements Bundlable {

	private static final String POS = "pos";
	private              int    pos = -1;
	
	public LevelObjectSprite sprite;

	public LevelObject(int pos) {
		this.pos = pos;
	}

	abstract public int image();

	abstract void setupFromJson(Level level, JSONObject obj) throws JSONException;

	public boolean interact(Hero hero ) {return true;}
	public boolean stepOn(Hero hero) {return true;}

	protected void remove() {
		Dungeon.level.remove(this);
		sprite.kill();
	}

	public void burn() {}
	public void freeze() {}
	public void poison(){}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		setPos(bundle.getInt( POS ));
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		bundle.put( POS, getPos());
	}
	
	public boolean dontPack() {
		return false;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public abstract String desc();

	public abstract String name();

	public String texture(){
		return "levelObjects/objects.png";
	}
}
