package com.watabou.pixeldungeon.actors.blobs;

import com.nyrds.LuaInterface;
import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.hero.Doom;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.BArray;
import com.watabou.pixeldungeon.utils.Utils;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaValue;

/**
 * One blob for every data-kind blob (was one java class per kind). The hot
 * loops stay here - the executor chosen by the kind script's desc.mode runs
 * the O(area) walk (4-neighbor diffusion / decay / persist / the two fire
 * walkers) plus the cheap java mechanics (cross-blob cancel pairs); every
 * per-covered-cell mechanic is dispatched ONCE per tick as a batched
 * affect(self, blob, cells) script call, so a kind hook costs one invocation
 * per tick no matter how many cells are covered.
 *
 * Lua hooks (self = the script table, blob = this):
 *   desc(self) -> { mode, cancelsWith, seedMode, tileDesc }
 *     mode: "diffusion" (gases + regrowth) | "decay" (web) |
 *           "persist" (darkness, foliage) | "fire" | "liquidFlame"
 *   affect(self, blob, cells)  -> per-covered-cell mechanics (batched; the
 *                                 cells arrive as a java int[] iterated
 *                                 1-based; blob.cur holds the tick's volumes)
 *   after(self, blob)          -> once per tick when volume > 0 (foliage
 *                                 journal/shadows)
 *   onHeroDeath(self, blob)    -> Doom (the ToxicGas death report)
 *
 * Emitter pours and all rendering stay java-side (the BlobEmitters switch),
 * exactly like every other particle layer.
 */
@LuaInterface
public class ScriptBlob extends Blob implements Doom {

    private final String kind;
    private final transient LuaScript script;

    private String mode = MODE_DIFFUSION;
    private String cancelsWith = Utils.EMPTY_STRING;
    private String tileDescId = Utils.EMPTY_STRING;
    private boolean seedMax;

    public static final String MODE_DIFFUSION   = "diffusion";
    public static final String MODE_DECAY       = "decay";
    public static final String MODE_PERSIST     = "persist";
    public static final String MODE_FIRE        = "fire";
    public static final String MODE_LIQUIDFLAME = "liquidFlame";

    // per-tick scratch for the fire walkers (which cells burn this tick)
    private boolean[] touched;

    @SneakyThrows
    public ScriptBlob(@NotNull String kind) {
        this.kind = kind;
        script = new LuaScript("scripts/blobs/" + kind, "scripts/lib/emptykind", this);

        if (script.hasMethod("desc")) {
            LuaValue desc = script.run("desc").checktable();
            mode = desc.rawget("mode").optjstring(MODE_DIFFUSION);
            cancelsWith = desc.rawget("cancelsWith").optjstring(Utils.EMPTY_STRING);
            tileDescId = desc.rawget("tileDesc").optjstring(Utils.EMPTY_STRING);
            seedMax = desc.rawget("seedMode").optjstring(Utils.EMPTY_STRING).equals("max");
        }
    }

    @Override
    public String getEntityKind() {
        return kind;
    }

    @Override
    public String getEntitySystem() {
        return "blob";
    }

    private void collect(boolean[] scratch, int pos) {
        if (scratch != null) {
            scratch[pos] = true;
        }
    }

    private void dispatchAffect() {
        if (!script.hasMethod("affect")) {
            return;
        }
        int covered = 0;
        if (touched != null) {
            for (boolean b : touched) {
                if (b) {
                    covered++;
                }
            }
        } else {
            for (int pos = 0; pos < cur.length; pos++) {
                if (cur[pos] > 0) {
                    covered++;
                }
            }
        }
        if (covered == 0) {
            return;
        }
        int[] cells = new int[covered];
        int i = 0;
        if (touched != null) {
            for (int pos = 0; pos < touched.length; pos++) {
                if (touched[pos]) {
                    cells[i++] = pos;
                }
            }
            touched = null;
        } else {
            for (int pos = 0; pos < cur.length; pos++) {
                if (cur[pos] > 0) {
                    cells[i++] = pos;
                }
            }
        }
        script.run("affect", cells);
    }

    private void dispatchAfter() {
        script.runOptionalNoRet("after", this);
    }

    @Override
    protected void evolve() {

        final Level level = Dungeon.level;

        switch (mode) {
            case MODE_DECAY: {
                // web: decay by one per tick, the roots via the script
                for (int pos = 0; pos < getLength(); pos++) {
                    int offv = cur[pos] > 0 ? cur[pos] - 1 : 0;
                    off[pos] = offv;
                    if (offv > 0) {
                        setVolume(getVolume() + offv);
                    }
                }
                dispatchAffect();
                break;
            }
            case MODE_PERSIST: {
                // darkness / foliage: values persist until cleared
                for (int pos = 0; pos < getLength(); pos++) {
                    if (cur[pos] > 0) {
                        off[pos] = cur[pos];
                        setVolume(getVolume() + off[pos]);
                    } else {
                        off[pos] = 0;
                    }
                }
                dispatchAffect();
                dispatchAfter();
                break;
            }
            case MODE_FIRE: {
                // fire: covered cells burn down, flammable neighbors ignite at 4
                boolean[] flammable = level.flammable;
                touched = new boolean[getLength()];
                int from = getWidth() + 1;
                int to = getLength() - getWidth() - 1;
                for (int pos = from; pos < to; pos++) {
                    int fire;
                    if (cur[pos] > 0) {
                        collect(touched, pos);
                        fire = cur[pos] - 1;
                    } else if (flammable[pos] && (cur[pos - 1] > 0 || cur[pos + 1] > 0
                            || cur[pos - getWidth()] > 0 || cur[pos + getWidth()] > 0)) {
                        fire = 4;
                        collect(touched, pos);
                    } else {
                        fire = 0;
                    }
                    setVolume(getVolume() + (off[pos] = fire));
                }
                dispatchAffect();
                break;
            }
            case MODE_LIQUIDFLAME: {
                // liquid flame: NEIGHBOURS5 spread into flammable cells, then a
                // full diffusion pass; everything touched burns (the spread
                // touches cur in place, exactly like the java original)
                boolean[] flammable = level.flammable;
                boolean[] notBlocking = BArray.not(BArray.or(level.solid, level.water, null), null);
                touched = new boolean[getLength()];
                java.util.Arrays.fill(off, 0);
                for (int i = 1; i < getHeight() - 1; i++) {
                    int from = i * getWidth() + 1;
                    int to = from + getWidth() - 2;
                    for (int pos = from; pos < to; pos++) {
                        for (int delta : Level.NEIGHBOURS5) {
                            int cell = pos + delta;
                            if (cur[pos] > 0 && flammable[cell]) {
                                cur[cell] += 10;
                                collect(touched, cell);
                            }
                        }
                    }
                }
                for (int i = 1; i < getHeight() - 1; i++) {
                    int from = i * getWidth() + 1;
                    int to = from + getWidth() - 2;
                    for (int pos = from; pos < to; pos++) {
                        int value;
                        if (notBlocking[pos]) {
                            int count = 1;
                            int sum = cur[pos];
                            for (int delta : Level.NEIGHBOURS4) {
                                int cell = pos + delta;
                                if (notBlocking[cell]) {
                                    sum += cur[cell];
                                    count++;
                                }
                            }
                            value = sum >= count ? (sum / count) - 1 : 0;
                        } else {
                            value = cur[pos] / 2;
                        }
                        off[pos] = value;
                        setVolume(getVolume() + value);
                    }
                }
                dispatchAffect();
                break;
            }
            case MODE_DIFFUSION:
            default: {
                // the 4-neighbor diffusion pass, then the batched
                // per-covered-cell mechanics, then the optional cross-blob
                // cancel pair (the original gas order: poison, then cancel)
                boolean[] notBlocking = BArray.not(level.solid, null);

                for (int i = 1; i < getHeight() - 1; i++) {

                    int from = i * getWidth() + 1;
                    int to = from + getWidth() - 2;

                    for (int pos = from; pos < to; pos++) {
                        if (notBlocking[pos]) {

                            int count = 1;
                            int sum = cur[pos];

                            if (notBlocking[pos - 1]) {
                                sum += cur[pos - 1];
                                count++;
                            }
                            if (notBlocking[pos + 1]) {
                                sum += cur[pos + 1];
                                count++;
                            }
                            if (notBlocking[pos - getWidth()]) {
                                sum += cur[pos - getWidth()];
                                count++;
                            }
                            if (notBlocking[pos + getWidth()]) {
                                sum += cur[pos + getWidth()];
                                count++;
                            }

                            int value = sum >= count ? (sum / count) - 1 : 0;
                            off[pos] = value;

                            setVolume(getVolume() + value);
                        } else {
                            off[pos] = 0;
                        }
                    }
                }

                dispatchAffect();

                if (!cancelsWith.isEmpty()) {
                    Blob other = level.blobAt(cancelsWith);
                    if (other != null) {
                        int[] par = other.cur;
                        for (int i = 0; i < getLength(); i++) {
                            int t = cur[i];
                            int p = par[i];
                            if (p >= t) {
                                setVolume(getVolume() - t);
                                cur[i] = 0;
                            } else {
                                other.setVolume(other.getVolume() - p);
                                par[i] = 0;
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    // fire/liquidFlame keep their burn mechanics reachable as a plain static -
    // the kind scripts call it per collected cell (was Fire.burn)
    @LuaInterface
    public static void burn(int pos) {
        Char ch = Actor.findChar(pos);
        if (ch != null) {
            CharUtils.ignite(ch);
        }
        final Level level = Dungeon.level;
        Heap heap = level.getHeap(pos);
        if (heap != null) {
            heap.burn();
        }
        LevelObject levelObject = level.getTopLevelObject(pos);
        if (levelObject != null) {
            levelObject.burn();
        }
        if (pos == level.entrance || level.isExit(pos)) {
            return;
        }
        if (level.flammable[pos]) {
            level.set(pos, Terrain.EMBERS);
            GameScene.updateMapPair(pos);
            if (Dungeon.isCellVisible(pos)) {
                GameScene.discoverTile(pos);
            }
            Dungeon.observe();
        }
    }

    @Override
    public void seed(int cell, int amount) {
        checkSeedCell(cell);
        if (seedMax) {
            // web semantics: seeding raises the cell to amount, never past it
            int diff = amount - cur[cell];
            if (diff > 0) {
                cur[cell] = amount;
                setVolume(getVolume() + diff);
            }
            return;
        }
        cur[cell] += amount;
        setVolume(getVolume() + amount);
    }

    @Override
    public void use(BlobEmitter emitter) {
        super.use(emitter);
        BlobEmitters.configure(kind, emitter);
    }

    @Override
    public String tileDesc() {
        if (tileDescId.isEmpty()) {
            return null;
        }
        return StringsManager.maybeId(tileDescId);
    }

    @Override
    public void onHeroDeath() {
        script.runOptionalNoRet("onHeroDeath");
    }
}
