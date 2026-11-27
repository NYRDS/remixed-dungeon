package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.util.ModdingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Blob kind registry: every scripts/blobs/<Kind>.lua module registers a kind
 * served by ScriptBlob (the scan, like buffs/items); the wave-2 java blobs
 * (the wells + the alchemy pot) stay class-constructed until their batch.
 * Legacy FQN bundle keys derive their simple name here via
 * Bundle.resolveByDerivedKind's exact-first tryResolvers - no aliases needed.
 */
public class BlobFactory {

    static private final Map<String, Supplier<Blob>> blobList = new HashMap<>();
    static private final Set<String> scriptBlobs = new HashSet<>();
    static private boolean scanned;

    public static final String TOXIC_GAS      = "ToxicGas";
    public static final String PARALYTIC_GAS  = "ParalyticGas";
    public static final String CONFUSION_GAS  = "ConfusionGas";
    public static final String MIASMA_GAS     = "MiasmaGas";
    public static final String FIRE           = "Fire";
    public static final String LIQUID_FLAME   = "LiquidFlame";
    public static final String REGROWTH       = "Regrowth";
    public static final String WEB            = "Web";
    public static final String DARKNESS       = "Darkness";
    public static final String FOLIAGE        = "Foliage";

    static private synchronized void initKinds() {
        if (scanned) {
            return;
        }
        scanned = true;

        for (String file : ModdingMode.listResources("scripts/blobs", (dir, name) -> name.endsWith(".lua"))) {
            String kind = file.replace(".lua", "");
            scriptBlobs.add(kind);
            blobList.put(kind, () -> new ScriptBlob(kind));
        }

        // wave-2 java blobs (heavy item/char coupling; their batch converts
        // them to scripts)
        blobList.put("WellWater", WellWater::new);
        blobList.put("WaterOfHealth", WaterOfHealth::new);
        blobList.put("WaterOfAwareness", WaterOfAwareness::new);
        blobList.put("WaterOfTransmutation", WaterOfTransmutation::new);
        blobList.put("Alchemy", Alchemy::new);
    }

    public static boolean hasKind(String kind) {
        initKinds();
        return blobList.containsKey(kind);
    }

    public static Blob byKind(String kind) {
        if (!hasKind(kind)) {
            return null;
        }
        return blobList.get(kind).get();
    }

    public static Set<String> getBlobNames() {
        initKinds();
        return blobList.keySet();
    }
}
