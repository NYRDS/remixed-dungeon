/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.StringsManager;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndStory;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Codex extends Item {

	private static final String TXT_BLINDED	= Game.getVar(R.string.Codex_Blinded);

	public static final String AC_READ	= Game.getVar(R.string.Codex_ACRead);

	private static String idTag   = "id";
	private static String textTag = "text";
	private int    maxId = 0;
	
	private int    id;
	private String text;

	public Codex(){
		stackable = false;
		image     = ItemSpriteSheet.CODEX;
		//TODO Need rework this. Transifex just hates string-arrays
		maxId     = Game.getVars(R.array.Codex_Story).length;
		id        = Random.Int(maxId);
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_READ );
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_READ )) {

			if (hero.buff( Blindness.class ) != null) {
				GLog.w( TXT_BLINDED );
				return;
			}

			setCurUser(hero);
			if(text != null) {
				WndStory.showCustomStory(text);
			} else {
				WndStory.showCustomStory(Game.getVars(R.array.Codex_Story)[id]);
			}
			return;
		}

		super.execute( hero, action );
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}

	public void fromJson(JSONObject itemDesc) throws JSONException {
		super.fromJson(itemDesc);

		text = StringsManager.maybeId(itemDesc.optString(textTag));
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		id   = bundle.getInt( idTag );
		if(!(id < maxId)){
			id = Random.Int(maxId);
		}
		text = bundle.optString(textTag,null);
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(idTag, id);
		bundle.put(textTag, text);
	}
	
	@Override
	public int price(){
		return 5;
	}
}
