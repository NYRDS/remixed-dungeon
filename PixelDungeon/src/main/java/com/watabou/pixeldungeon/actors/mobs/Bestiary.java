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
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.android.util.JsonHelper;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class Bestiary {

	private static final String FEELINGS = "Feelings";
	private static JSONObject bestiaryData;

	static {
		bestiaryData = JsonHelper.readJsonFromAsset("levelsDesc/Bestiary.json");
	}

	public static Mob mob() {
		try {

			String idString = DungeonGenerator.getCurrentLevelId();

			if (bestiaryData.has(FEELINGS)) {

				JSONObject Feelings = bestiaryData.getJSONObject(FEELINGS);

				if (Random.Float(1) < Feelings.optDouble("Chance", 0.2)) {

					String feeling = DungeonGenerator.getLevelFeeling(idString).name();

					if (Feelings.has(feeling)) {
						return getMob(Feelings.getJSONObject(feeling));
					}
				}
			}

			JSONObject levelDesc = bestiaryData.getJSONObject(DungeonGenerator.getCurrentLevelKind());

			if (!levelDesc.has(idString)) {
				idString = Integer.toString(DungeonGenerator.getCurrentLevelDepth());

				if (!levelDesc.has(idString)) {
					idString = "any";
				}
			}

			return getMob(levelDesc.getJSONObject(idString));

		} catch (JSONException e) {
			Game.toast(e.getMessage());
		}
		return MobFactory.mobRandom();
	}

	private static Mob getMob(JSONObject depthDesc) throws JSONException {
		ArrayList<Float> chances = new ArrayList<>();
		ArrayList<String> names = new ArrayList<>();

		Iterator<?> keys = depthDesc.keys();

		while (keys.hasNext()) {
			String mobClassName = (String) keys.next();
			names.add(mobClassName);
			float chance = (float) depthDesc.getDouble(mobClassName);
			chances.add(chance);
		}
		String selectedMobClass = (String) names.toArray()[Random.chances(chances.toArray(new Float[chances.size()]))];
		return MobFactory.mobByName(selectedMobClass);
	}
}
