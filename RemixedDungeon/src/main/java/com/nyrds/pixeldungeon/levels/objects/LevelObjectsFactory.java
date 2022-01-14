package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.LuaInterface;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModError;
import com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker;
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

import java.util.HashMap;

import clone.org.json.JSONException;
import clone.org.json.JSONObject;
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
		} catch (InstantiationException e) {
			throw new TrackedRuntimeException(Utils.EMPTY_STRING, e);
		} catch (IllegalAccessException e) {
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

}
