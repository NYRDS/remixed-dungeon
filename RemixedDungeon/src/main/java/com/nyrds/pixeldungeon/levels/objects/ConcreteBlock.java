package com.nyrds.pixeldungeon.levels.objects;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.levels.Level;

import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by mike on 01.07.2016.
 */
public class ConcreteBlock extends LevelObject {

	@Packable
	@Getter
	@Setter
	private int requiredStr = 10;

	@Keep
	public ConcreteBlock() {
		this(-1);
	}

	public ConcreteBlock(int pos) {
		super(pos);
		imageIndex = 0;
	}

	public ConcreteBlock(int pos, int str) {
		this(pos);
		requiredStr = str;
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) {
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
		return hero.effectiveSTR() >= requiredStr && super.push(hero);
	}

	@Override
	public String desc() {
        return String.format(StringsManager.getVar(R.string.ConcreteBlock_Description), requiredStr);
	}

	@Override
	public String name() {
        return StringsManager.getVar(R.string.ConcreteBlock_Name);
    }

	@Override
	public boolean affectLevelObjects() {
		return true;
	}

	@Override
	public boolean nonPassable(Char ch) {
		return true;
	}
}
