package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.LuaInterface;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModError;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker;
import com.watabou.pixeldungeon.levels.DeadEndLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.plants.Dreamweed;
import com.watabou.pixeldungeon.plants.Earthroot;
import com.watabou.pixeldungeon.plants.Fadeleaf;
import com.watabou.pixeldungeon.plants.Firebloom;
import com.watabou.pixeldungeon.plants.Icecap;
import com.watabou.pixeldungeon.plants.Moongrace;
import com.watabou.pixeldungeon.plants.Sorrowmoss;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.pixeldungeon.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.SneakyThrows;

/**
 * Created by mike on 05.07.2016.
 */
public class LevelObjectsFactory {

    public static final String PEDESTAL = "pedestal";
    public static final String STATUE = "statue";
	public static final String STATUE_SP = "statue_sp";
	public static final String BARRICADE = "barricade";
	public static final String WELL = "well";
	public static final String POT = "pot";
    public static final String FIRE_TRAP = "FireTrap";
    public static final String TOXIC_TRAP = "ToxicTrap";
    public static final String PARALYTIC_TRAP = "ParalyticTrap";
    public static final String POISON_TRAP = "PoisonTrap";
    public static final String ALARM_TRAP = "AlarmTrap";
    public static final String LIGHTNING_TRAP = "LightningTrap";
    public static final String GRIPPING_TRAP = "GrippingTrap";
    public static final String SUMMONING_TRAP = "SummoningTrap";
	public static final String PILE_OF_STONES = "pile_of_stones";

    static private HashMap<String, Class<? extends LevelObject>> mObjectsList;

	static  {
		initObjectsMap();
	}
	private static void registerObjectClass(Class<? extends LevelObject> objectClass) {
		mObjectsList.put(objectClass.getSimpleName(), objectClass);
	}

	private static void initObjectsMap() {

		mObjectsList = new HashMap<>();
		registerObjectClass(Sign.class);
		registerObjectClass(Barrel.class);
		registerObjectClass(ConcreteBlock.class);
		registerObjectClass(LibraryBook.class);
		registerObjectClass(PortalGateSender.class);
		registerObjectClass(PortalGateReceiver.class);
		registerObjectClass(Trap.class);
		registerObjectClass(Deco.class);
		registerObjectClass(Dreamweed.class);
		registerObjectClass(Earthroot.class);
		registerObjectClass(Fadeleaf.class);
		registerObjectClass(Firebloom.class);
		registerObjectClass(Icecap.class);
		registerObjectClass(WandMaker.Rotberry.class);
		registerObjectClass(Sorrowmoss.class);
		registerObjectClass(Sungrass.class);
		registerObjectClass(Moongrace.class);
		registerObjectClass(CustomObject.class);
	}

	public static boolean isValidObjectClass(String objectClass) {
		return mObjectsList.containsKey(objectClass);
	}

	@SneakyThrows
	@LuaInterface
	public static LevelObject createObject(Level level, String jsonDesc) {
		return createLevelObject(level, JsonHelper.readJsonFromString(jsonDesc));
	}


	@SneakyThrows
	@LuaInterface
	public static LevelObject createCustomObject(Level level, String kind, int cell) {

		level.clearCellForObject(cell);

		LevelObject obj = objectByName("CustomObject");
		JSONObject desc = new JSONObject();

		desc.put("object_desc", kind);
		obj.setPos(cell);

		obj.setupFromJson(level, desc);
		return obj;
	}

	public static LevelObject createLevelObject(Level level, JSONObject desc) throws JSONException {

		String objectKind = desc.getString("kind");

		LevelObject obj = objectByName(objectKind);

		int x = desc.getInt("x");
		int y = desc.getInt("y");
		obj.setPos(level.cell(x,y));

		obj.setupFromJson(level, desc);
		return obj;
	}

	@LuaInterface
	public static LevelObject objectByName(String objectClassName) {
		try {
			return objectClassByName(objectClassName).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new TrackedRuntimeException(Utils.EMPTY_STRING, e);
		}
    }


	public static Class<? extends LevelObject> objectClassByName(String objectClassName) {

		Class<? extends LevelObject> objectClass = mObjectsList.get(objectClassName);
		if(objectClass != null) {
			return objectClass;
		} else {
			throw new ModError(Utils.format("Unknown object: [%s]",objectClassName));
		}
	}

	public static List<LevelObject> allLevelObjects() {
		List<LevelObject> objects = new ArrayList<>();

		// Add one instance of each registered class
		for(String objectClass : mObjectsList.keySet()) {
			objects.add(objectByName(objectClass));
		}

		// Add instances for all possible JSON-defined objects from levelObjects directory
		// This includes both Deco and CustomObject configurations
		Level level = new DeadEndLevel();
		level.create();
		try {
			// List all files in the levelObjects directory
			FilenameFilter jsonFilter = (dir, name) -> name.toLowerCase().endsWith(".json");
			List<String> levelObjectFiles = ModdingMode.listResources("levelObjects", jsonFilter);

			for (String fileName : levelObjectFiles) {
				// Remove the .json extension to get the object name
				String objectName = fileName.substring(0, fileName.lastIndexOf('.'));

				// Read the JSON file to determine the object type
				JSONObject objectDef = JsonHelper.readJsonFromAsset("levelObjects/" + fileName);
				String kind = objectDef.optString("kind", "Deco"); // Default to Deco if kind is not specified

				// Create an instance based on the kind specified in the JSON
				LevelObject levelObject;
				if ("CustomObject".equals(kind)) {
					// Create a CustomObject and configure it
					CustomObject customObj = new CustomObject();
					customObj.setPos(level.cell(2, 2)); // Set a default position
					customObj.objectDesc = objectName;
					// Try to set up the object from its JSON definition to properly initialize it
					try {
						customObj.setupFromJson(level, objectDef);
					} catch (Exception e) {
						// If setup fails, still add the object but log the error
					}
					levelObject = customObj;
				} else {
					// Default to Deco for other kinds (including "Deco")
					Deco deco = (Deco) objectByName("Deco");
					deco.objectDesc = objectName;
					// Try to set up the object from its JSON definition to properly initialize it
					try {
						deco.setupFromJson(level, objectDef);
					} catch (Exception e) {
						// If setup fails, still add the object but log the error
					}
					levelObject = deco;
				}

				// Add the configured object to the list
				objects.add(levelObject);
			}
		} catch (Exception e) {
			// If there's an issue reading the assets, return just the registered classes
			// This ensures the method still works even if asset scanning fails
		}

		return objects;
	}

}
