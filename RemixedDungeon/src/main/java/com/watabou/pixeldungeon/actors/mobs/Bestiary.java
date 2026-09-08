
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.pixeldungeon.utils.DungeonGenerator;
import com.nyrds.platform.game.Game;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModError;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class Bestiary {

	private static final String FEELINGS = "Feelings";
	private static final JSONObject bestiaryData;
	private static JSONObject Feelings;
	private static double feelingChance;

	// mob kind -> sum(depth * weight), sum(weight) over Bestiary entries
	private static final Map<String, double[]> depthSums = new HashMap<>();
	private static boolean depthsScanned;

	static {
		bestiaryData = JsonHelper.readJsonFromAsset("levelsDesc/Bestiary.json");

		if(bestiaryData.has(FEELINGS)) {
			Feelings = bestiaryData.optJSONObject(FEELINGS);
			feelingChance = Feelings.optDouble("Chance", 0.2);
		}

        selfTest(bestiaryData);
	}

	// typical dungeon depth of a mob kind: weighted mean of the depths it
	// spawns at, rounded up. 0 when the kind is absent from the bestiary.
	public static int typicalDepth(String mobKind) {
		scanDepths();
		double[] sums = depthSums.get(mobKind);
		if (sums == null || sums[1] <= 0) {
			return 0;
		}
		return (int) Math.ceil(sums[0] / sums[1]);
	}

	// initial baseStr pacing: follows the armor tiers a hero meets at that depth
	// (typicalSTR 9/11/13/15/17), never below the hero's starting 10
	public static int baseStrFor(String mobKind) {
		int depth = typicalDepth(mobKind);
		return Math.max(10, 7 + 2 * ((depth + 4) / 5));
	}

	private static synchronized void scanDepths() {
		if (depthsScanned) {
			return;
		}
		depthsScanned = true;

		Iterator<String> kinds = bestiaryData.keys();
		while (kinds.hasNext()) {
			String kind = kinds.next();
			if (kind.equals(FEELINGS)) {
				continue;
			}
			JSONObject levelDesc = bestiaryData.optJSONObject(kind);
			if (levelDesc == null) {
				continue;
			}
			Iterator<String> levels = levelDesc.keys();
			while (levels.hasNext()) {
				String levelKey = levels.next();
				int depth;
				try {
					depth = Integer.parseInt(levelKey);
				} catch (NumberFormatException e) {
					continue; // "any" and named level ids carry no depth info
				}
				JSONObject atDepth = levelDesc.optJSONObject(levelKey);
				if (atDepth == null) {
					continue;
				}
				Iterator<String> mobs = atDepth.keys();
				while (mobs.hasNext()) {
					String mobKind = mobs.next();
					double weight = atDepth.optDouble(mobKind, 0);
					if (weight <= 0) {
						continue;
					}
					double[] sums = depthSums.get(mobKind);
					if (sums == null) {
						sums = new double[2];
						depthSums.put(mobKind, sums);
					}
					sums[0] += depth * weight;
					sums[1] += weight;
				}
			}
		}
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

		} catch (Exception e) {
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
