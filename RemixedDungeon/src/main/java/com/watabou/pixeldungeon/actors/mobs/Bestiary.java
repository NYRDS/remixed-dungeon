
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.platform.game.Game;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModError;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class Bestiary {

	private static final String FEELINGS = "Feelings";
	private static JSONObject bestiaryData;
	private static JSONObject Feelings;
	private static double feelingChance;

	static {
		bestiaryData = JsonHelper.readJsonFromAsset("levelsDesc/Bestiary.json");

		if(bestiaryData.has(FEELINGS)) {
			Feelings = bestiaryData.optJSONObject(FEELINGS);
			feelingChance = Feelings.optDouble("Chance", 0.2);
		}

        selfTest(bestiaryData);
	}

	private static String currentLevelId;

	private static JSONObject currentLevelBestiary;
	private static JSONObject currentLevelFeelingBestiary;

	public static Mob mob(Level level) {
		try {

			if(level.levelId.equals(currentLevelId) && currentLevelBestiary != null) {
				return getMobFromCachedData();
			}

			cacheLevelData(level);

			return getMobFromCachedData();

		} catch (JSONException e) {
			ModError.doReport("No bestiary for "+level.levelId, e);
			return MobFactory.mobByName("Rat");
		}
	}

	public static void selfTest(JSONObject root) {
	    Iterator<String> keyI = root.keys();
	    while (keyI.hasNext()) {
	        String key = keyI.next();

	        if(key.equals("Chance")) {
	        	continue;
			}

	        double chances = root.optDouble(key,-1);
	        if(chances>0) {
	            if(!MobFactory.hasMob(key)) {
	                ModError.doReport("missing mob class: "+key+" found in Bestiary.json", new Exception());
                }
	            continue;
            }
	        JSONObject childObject = root.optJSONObject(key);
	        if(childObject!=null) {
	            selfTest(childObject);
            }
        }
    }

	private static void cacheLevelData(Level level) throws JSONException {
		currentLevelId = level.levelId;

		if(Feelings!=null) {
			String feeling = level.getFeeling().name();
			currentLevelFeelingBestiary = Feelings.optJSONObject(feeling);
		}

		JSONObject levelDesc = bestiaryData.getJSONObject(DungeonGenerator.getCurrentLevelKind());

		if (!levelDesc.has(currentLevelId)) {
			currentLevelId = Integer.toString(DungeonGenerator.getCurrentLevelDepth());

			if (!levelDesc.has(currentLevelId)) {
				currentLevelId = "any";
			}
		}

		currentLevelBestiary = levelDesc.getJSONObject(currentLevelId);
	}

	private static Mob getMobFromCachedData() throws JSONException {
		if(currentLevelFeelingBestiary!= null) {
			if(Random.Float(1) < feelingChance) {
				return getMob(currentLevelFeelingBestiary);
			}
		}
		return getMob(currentLevelBestiary);
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

		String selectedMobClass = "Rat";

		if(!chances.isEmpty()) {
			selectedMobClass = (String) names.toArray()[Random.chances(chances.toArray(new Float[0]))];
		}	else {
			Game.toast("Bad bestiary desc: %s", depthDesc.toString());
		}
		return MobFactory.mobByName(selectedMobClass);
	}
}
