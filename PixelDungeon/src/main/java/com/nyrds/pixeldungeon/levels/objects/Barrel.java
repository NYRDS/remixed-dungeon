package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.android.util.ModdingMode;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.potions.PotionOfLiquidFlame;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mike on 01.07.2016.
 */
public class Barrel extends LevelObject {

	public Barrel() {
		super(-1);
	}

	public Barrel(int pos) {
		super(pos);
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
	}

	@Override
	public boolean pushable() {
		return true;
	}

	@Override
	public boolean push(Hero hero) {
		Level level = Dungeon.level;
		int hx = level.cellX(hero.getPos());
		int hy = level.cellY(hero.getPos());

		int x = level.cellX(getPos());
		int y = level.cellY(getPos());

		int dx = x - hx;
		int dy = y - hy;

		if (dx * dy != 0) {
			return false;
		}

		int nextCell = level.cell(x + dx, y + dy);

		if(!level.cellValid(nextCell)) {
			return false;
		}

		if (!level.passable[nextCell] || level.getLevelObject(nextCell)!=null) {
			return false;
		} else {
			level.objectPress(nextCell,this);
			setPos(nextCell);
			level.levelObjectMoved(this);
		}

		return true;
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
	}

	@Override
	public void burn() {
		sprite.playAnim(10, false, new Callback() {
			@Override
			public void call() {
				remove();

			}
		}, image()+0,  image()+1,  image()+2,  image()+3,  image()+4);
		new PotionOfLiquidFlame().shatter(getPos());
	}

	@Override
	public void bump() {
		burn();
	}

	@Override
	public String desc() {

		if(ModdingMode.isHalloweenEvent()) {
			return Game.getVar(R.string.Barrel_Pumpkin_Desc);
		} else {
			return Game.getVar(R.string.Barrel_Desc);
		}

	}

	@Override
	public String name() {
		if(ModdingMode.isHalloweenEvent()) {
			return Game.getVar(R.string.Barrel_Pumpkin_Name);
		} else {
			return Game.getVar(R.string.Barrel_Name);
		}
	}

	@Override
	public int image() {

		if(ModdingMode.isHalloweenEvent()) {
			return 0;
		} else {
			return 8;
		}
	}

	@Override
	public String texture() {
		return "levelObjects/barrels.png";
	}
}
