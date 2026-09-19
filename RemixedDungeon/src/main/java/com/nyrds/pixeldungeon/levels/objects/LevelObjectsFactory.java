package com.nyrds.pixeldungeon.levels.objects;

import com.nyrds.LuaInterface;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModError;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.utils.Utils;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

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
    public static final String SIGN = "Sign";
    public static final String BARREL = "Barrel";
    public static final String LIBRARY_BOOK = "LibraryBook";
    public static final String PORTAL_GATE_SENDER = "PortalGateSender";
    public static final String PORTAL_GATE_RECEIVER = "PortalGateReceiver";
    public static final String FIRE_TRAP = "FireTrap";
    public static final String TOXIC_TRAP = "ToxicTrap";
    public static final String PARALYTIC_TRAP = "ParalyticTrap";
    public static final String POISON_TRAP = "PoisonTrap";
    public static final String ALARM_TRAP = "AlarmTrap";
    public static final String LIGHTNING_TRAP = "LightningTrap";
    public static final String GRIPPING_TRAP = "GrippingTrap";
    public static final String SUMMONING_TRAP = "SummoningTrap";
    public static final String PILE_OF_STONES = "pile_of_stones";
    public static final String CONCRETE_BLOCK = "ConcreteBlock";

    static private HashMap<String, Class<? extends LevelObject>> mObjectsList;

    static {
        initObjectsMap();
    }

    private static void registerObjectClass(Class<? extends LevelObject> objectClass) {
        mObjectsList.put(objectClass.getSimpleName(), objectClass);
    }

    private static void registerObjectClassByName(String kind, Class<? extends LevelObject> objectClass) {
        mObjectsList.put(kind, objectClass);
    }

    private static void initObjectsMap() {

        mObjectsList = new HashMap<>();
        // batch 21: furniture kinds are data-defined; kind strings stay stable
        registerObjectClassByName(SIGN, CustomObject.class);
        registerObjectClassByName(BARREL, CustomObject.class);
        registerObjectClassByName(CONCRETE_BLOCK, CustomObject.class);
        registerObjectClassByName(LIBRARY_BOOK, CustomObject.class);
        registerObjectClassByName(PORTAL_GATE_SENDER, CustomObject.class);
        registerObjectClassByName(PORTAL_GATE_RECEIVER, CustomObject.class);
        registerObjectClass(Trap.class);
        registerObjectClass(Deco.class);
        registerObjectClass(CustomObject.class);

        // batch 20: all plants are data-defined; Plant serves every kind,
        // behavior lives in scripts/plants/<Kind>.lua
        registerObjectClass(Plant.class);
        registerObjectClassByName("Dreamweed", Plant.class);
        registerObjectClassByName("Earthroot", Plant.class);
        registerObjectClassByName("Fadeleaf", Plant.class);
        registerObjectClassByName("Firebloom", Plant.class);
        registerObjectClassByName("Icecap", Plant.class);
        registerObjectClassByName("Rotberry", Plant.class);
        registerObjectClassByName("Sorrowmoss", Plant.class);
        registerObjectClassByName("Sungrass", Plant.class);
        registerObjectClassByName("Moongrace", Plant.class);

        // mods can add plants by shipping scripts/plants/<Kind>.lua
        // (+ plantsDesc/<Kind>.json for the sprite frame)
        for(String plantScript: ModdingMode.listResources("scripts/plants", (dir, name) -> name.endsWith(".lua"))) {
            String plantKind = plantScript.replace(".lua", Utils.EMPTY_STRING);
            mObjectsList.putIfAbsent(plantKind, Plant.class);
        }
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
        return createCustomObject(level, kind, cell, null);
    }

    /** Kind-served object with per-instance data (rides LevelObject.data). */
    @SneakyThrows
    @LuaInterface
    public static LevelObject createCustomObject(Level level, String kind, int cell, String data) {

        level.clearCellForObject(cell);

        LevelObject obj = objectByName("CustomObject");
        JSONObject desc = new JSONObject();

        desc.put("object_desc", kind);
        if (data != null) {
            desc.put("data", data);
        }

        obj.setPos(cell);

        obj.setupFromJson(level, desc);
        return obj;
    }

    public static LevelObject createLevelObject(Level level, JSONObject desc) throws JSONException {

        String objectKind = desc.getString("kind");

        LevelObject obj = objectByName(objectKind);

        int x = desc.getInt("x");
        int y = desc.getInt("y");
        obj.setPos(level.cell(x, y));

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

    /**
     * Gated resolver for save-restore: null unless the object kind is registered.
     */
    @Nullable
    public static LevelObject tryByName(String objectClassName) {
        if (!isValidObjectClass(objectClassName)) {
            return null;
        }
        try {
            return objectByName(objectClassName);
        } catch (Exception e) {
            EventCollector.logException(e, objectClassName);
            return null;
        }
    }


    public static Class<? extends LevelObject> objectClassByName(String objectClassName) {

        Class<? extends LevelObject> objectClass = mObjectsList.get(objectClassName);
        if (objectClass != null) {
            return objectClass;
        } else {
            throw new ModError(Utils.format("Unknown object: [%s]", objectClassName));
        }
    }


    public static List<LevelObject> allLevelObjects(@NotNull Level levelContext) {
        List<LevelObject> objects = new ArrayList<>();

        for (String objectClass : mObjectsList.keySet()) {
            LevelObject object = objectByName(objectClass);
            // seed the def so the listing can describe kind-served objects
            if (object instanceof CustomObject && object.getEntityKind() == null) {
                ((CustomObject) object).objectDesc = objectClass;
            }
            objects.add(object);
        }

        // Add all trap variants
        String[] trapKinds = {
            FIRE_TRAP, TOXIC_TRAP, PARALYTIC_TRAP, POISON_TRAP,
            ALARM_TRAP, LIGHTNING_TRAP, GRIPPING_TRAP, SUMMONING_TRAP
        };

        for (String trapKind : trapKinds) {
            Trap trap = (Trap) objectByName("Trap");
            trap.kind = trapKind;
            trap.setPos(levelContext.cell(1, 1)); // Set a default position
            trap.uses = 1;
            trap.targetCell = trap.getPos();
            trap.activatedByItem = true;
            trap.activatedByMob = true;
            objects.add(trap);
        }

        // Add script traps
        try {
            FilenameFilter luaFilter = (dir, name) -> name.toLowerCase().endsWith(".lua");
            List<String> scriptTrapFiles = ModdingMode.listResources("scripts/traps", luaFilter);

            for (String fileName : scriptTrapFiles) {
                String scriptName = fileName.substring(0, fileName.lastIndexOf('.'));

                Trap scriptTrap = (Trap) objectByName("Trap");
                scriptTrap.kind = "scriptFile";
                scriptTrap.script = "traps/" + scriptName; // Path to the Lua script
                scriptTrap.setPos(levelContext.cell(1, 2)); // Set a default position
                scriptTrap.uses = 1;
                scriptTrap.targetCell = scriptTrap.getPos();
                scriptTrap.activatedByItem = true;
                scriptTrap.activatedByMob = true;
                objects.add(scriptTrap);
            }
        } catch (Exception e) {
            EventCollector.logException(e);
        }

        try {
            FilenameFilter jsonFilter = (dir, name) -> name.toLowerCase().endsWith(".json");
            List<String> levelObjectFiles = ModdingMode.listResources("levelObjects", jsonFilter);

            for (String fileName : levelObjectFiles) {
                String objectName = fileName.substring(0, fileName.lastIndexOf('.'));

                JSONObject objectDef = JsonHelper.readJsonFromAsset("levelObjects/" + fileName);
                String kind = objectDef.optString("kind", "Deco");

                LevelObject levelObject;
                if ("CustomObject".equals(kind)) {
                    CustomObject customObj = new CustomObject();
                    customObj.setPos(levelContext.cell(2, 2));
                    customObj.objectDesc = objectName;
                    try {
                        customObj.setupFromJson(levelContext, objectDef);
                    } catch (Exception e) {
                        EventCollector.logException(e);
                    }
                    levelObject = customObj;
                } else {
                    Deco deco = (Deco) objectByName("Deco");
                    deco.objectDesc = objectName;

                    try {
                        deco.setupFromJson(levelContext, objectDef);
                    } catch (Exception e) {
                        EventCollector.logException(e);
                    }
                    levelObject = deco;
                }
                objects.add(levelObject);
            }
        } catch (Exception e) {
            EventCollector.logException(e);
        }

        return objects;
    }

}
