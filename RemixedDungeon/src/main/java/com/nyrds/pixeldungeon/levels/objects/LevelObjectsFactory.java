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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by mike on 05.07.2016.
 */
public class LevelObjectsFactory {

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

	@LuaInterface
	public static LevelObject createObject(Level level, String jsonDesc) throws JSONException {
		return createObject(level, JsonHelper.readJsonFromString(jsonDesc));
	}


	public static LevelObject createObject(Level level, String kind, int cell) {
		LevelObject obj = objectByName(kind);
		obj.setPos(cell);
		return obj;
	}

	public static LevelObject createObject(Level level, JSONObject desc) throws JSONException {

		String objectKind = desc.getString("kind");

		LevelObject obj = objectByName(objectKind);

		int x = desc.getInt("x");
		int y = desc.getInt("y");

		obj.setupFromJson(level, desc);
		obj.setPos(level.cell(x,y));
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
