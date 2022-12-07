package com.nyrds.retrodungeon.levels.objects;

import com.nyrds.android.util.TrackedRuntimeException;
import com.watabou.pixeldungeon.levels.Level;
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
	}

	public static boolean isValidObjectClass(String objectClass) {
		return mObjectsList.containsKey(objectClass);
	}


	public static LevelObject createObject(Level level, JSONObject desc) throws JSONException {

		String objectKind = desc.getString("kind");

		LevelObject obj = objectByName(objectKind);

		int x = desc.getInt("x");
		int y = desc.getInt("y");
		obj.setPos(level.cell(x,y));
		obj.setupFromJson(level, desc);
		return obj;
	}

	public static LevelObject objectByName(String objectClassName) {
		try {
			return objectClassByName(objectClassName).newInstance();
		} catch (InstantiationException e) {
			throw new TrackedRuntimeException("", e);
		} catch (IllegalAccessException e) {
			throw new TrackedRuntimeException("", e);
		}
	}


	public static Class<? extends LevelObject> objectClassByName(String objectClassName) {

		Class<? extends LevelObject> objectClass = mObjectsList.get(objectClassName);
		if(objectClass != null) {
			return objectClass;
		} else {
			throw new TrackedRuntimeException(Utils.format("Unknown object: [%s]",objectClassName));
		}
	}

}
