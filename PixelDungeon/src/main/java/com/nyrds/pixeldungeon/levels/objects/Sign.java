package com.nyrds.pixeldungeon.levels.objects;

import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.windows.WndMessage;
import com.watabou.utils.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mike on 01.07.2016.
 */
public class Sign extends LevelObject {

	private static final String TEXT = "text";
	private String signText;

	public Sign(){
		super(-1);
	}

	public Sign(int pos, String text) {
		super(pos);
		signText = text;
	}

	@Override
	void setupFromJson(Level level, JSONObject obj) throws JSONException {
		signText = StringsManager.maybeId(obj.getString(TEXT));
	}

	@Override
	public boolean interact(Hero hero) {
		GameScene.show(new WndMessage(signText));

		return super.interact(hero);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		signText = (bundle.getString( TEXT ));

	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put( TEXT, signText);
	}

	@Override
	public void burn() {
		remove();
		int oldTile = Dungeon.level.map[getPos()];
		Dungeon.level.set(getPos(),Terrain.EMBERS);
		GameScene.discoverTile(getPos(),oldTile);
	}

	@Override
	public String desc() {
		return Dungeon.level.tileDesc(Terrain.SIGN);
	}

	@Override
	public String name() {
		return Dungeon.level.tileName(Terrain.SIGN);
	}

	@Override
	public int image() {
		return 0;
	}
}
