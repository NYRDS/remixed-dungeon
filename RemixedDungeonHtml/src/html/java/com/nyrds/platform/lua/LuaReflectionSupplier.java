package com.nyrds.platform.lua;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.teavm.classlib.ReflectionContext;
import org.teavm.classlib.ReflectionSupplier;
import org.teavm.model.MethodDescriptor;

/**
 * Tells TeaVM which classes need reflective access because they are accessed
 * dynamically from Lua scripts via luajava.bindClass() / newInstance().
 *
 * - isClassFoundByName: makes Class.forName() work for these classes
 * - getAccessibleMethods: exposes constructors so newInstance() can call them
 *
 * We only expose constructors (<init>), not all methods, to avoid pulling in
 * transitive dependencies that TeaVM's classlib doesn't support (e.g. java.text.Collator).
 */
public class LuaReflectionSupplier implements ReflectionSupplier {

    /** Classes that need Class.forName() to work (bindClass) */
    private static final Set<String> KNOWN_CLASSES = new HashSet<>();

    /** Classes that need constructors exposed (newInstance) */
    private static final Set<String> INSTANTIABLE_CLASSES = new HashSet<>();

    static {
        // Classes accessed via luajava.newInstance() — need constructors
        INSTANTIABLE_CLASSES.add("com.nyrds.pixeldungeon.utils.CharsList");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.items.wands.WandOfBlink");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.items.wands.WandOfTelekinesis");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.items.wands.WandOfFirebolt");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.windows.WndMessage");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.windows.WndStory");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.windows.WndQuest");
        INSTANTIABLE_CLASSES.add("com.nyrds.pixeldungeon.windows.WndOptionsLua");
        INSTANTIABLE_CLASSES.add("com.nyrds.pixeldungeon.windows.WndShopOptions");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.windows.WndChooseWay");
        INSTANTIABLE_CLASSES.add("com.watabou.noosa.Image");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.ui.Banner");
        INSTANTIABLE_CLASSES.add("com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor");
        INSTANTIABLE_CLASSES.add("com.watabou.noosa.tweeners.PosTweener");
        INSTANTIABLE_CLASSES.add("com.watabou.noosa.tweeners.JumpTweener");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.effects.CellEmitter");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.effects.BlobEmitter");
        INSTANTIABLE_CLASSES.add("com.watabou.noosa.particles.Emitter");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.effects.Speck");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.effects.SpellSprite");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.effects.MagicMissile");
        INSTANTIABLE_CLASSES.add("com.nyrds.pixeldungeon.effects.DeathStroke");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.effects.Wound");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.effects.Flare");
        INSTANTIABLE_CLASSES.add("com.watabou.pixeldungeon.effects.HighlightCell");
        INSTANTIABLE_CLASSES.add("com.nyrds.pixeldungeon.utils.Position");

        // All known classes — Class.forName() + bindClass support
        KNOWN_CLASSES.addAll(INSTANTIABLE_CLASSES);

        // Classes only accessed via bindClass (static methods) — no constructor needed
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.utils.GLog");
        KNOWN_CLASSES.add("com.nyrds.platform.game.RemixedDungeon");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.game.GameLoop");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.utils.DungeonGenerator");
        KNOWN_CLASSES.add("com.watabou.utils.PathFinder");
        KNOWN_CLASSES.add("com.nyrds.platform.audio.Sample");
        KNOWN_CLASSES.add("com.nyrds.platform.audio.MusicManager");
        KNOWN_CLASSES.add("com.nyrds.platform.util.StringsManager");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.CharUtils");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.game.ModQuirks");
        KNOWN_CLASSES.add("com.nyrds.util.Util");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.windows.WndBag");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.items.Treasury");

        // Buffs
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Buff");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Roots");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Paralysis");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Vertigo");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Invisibility");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Levitation");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Hunger");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Poison");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Frost");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Light");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Cripple");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Charm");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Blessed");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.MindVision");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.mechanics.buffs.Necrotism");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.mechanics.buffs.RageBuff");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Terror");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Amok");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Awareness");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Barkskin");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Sleep");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Slow");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Blindness");

        // Blobs
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Blob");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Fire");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Foliage");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.ConfusionGas");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.LiquidFlame");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.ParalyticGas");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Darkness");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Web");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.ToxicGas");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.MiasmaGas");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Regrowth");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.WaterOfHealth");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.WaterOfTransmutation");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.WaterOfAwareness");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Alchemy");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Freezing");

        // Mechanics
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.mechanics.Ballistica");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.Challenges");

        // Scenes & UI
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.scenes.GameScene");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.Dungeon");
        KNOWN_CLASSES.add("com.watabou.noosa.Camera");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.ai.MobAi");

        // Factories
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.items.common.ItemFactory");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.mobs.common.MobFactory");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.effects.EffectsFactory");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.mechanics.spells.SpellFactory");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.Effects");

        // Particle classes
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.FlameParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.SnowParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.ShaftParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.ShadowParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.DarknessParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.EarthParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.EnergyParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.FlowParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.LeafParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.PoisonParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.PurpleParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.SparkParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.WebParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.WindParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.WoolParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.BloodParticle");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.effects.particles.ElmoParticle");

        // Misc
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.Badges");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.items.ItemUtils");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.DungeonTilemap");
        KNOWN_CLASSES.add("com.nyrds.util.ModdingMode");
        KNOWN_CLASSES.add("com.nyrds.lua.LuaUtils");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.ui.QuickSlot");
        KNOWN_CLASSES.add("com.nyrds.platform.app.Input");
        KNOWN_CLASSES.add("com.watabou.utils.SystemTime");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.levels.Terrain");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.Actor");
        KNOWN_CLASSES.add("com.nyrds.pixeldungeon.alchemy.AlchemyRecipes");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.Journal");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.levels.features.Chasm");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.actors.mobs.Mob");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.items.Heap");
        KNOWN_CLASSES.add("com.watabou.pixeldungeon.ui.BuffIndicator");
    }

    @Override
    public boolean isClassFoundByName(ReflectionContext context, String name) {
        return KNOWN_CLASSES.contains(name);
    }

    @Override
    public Collection<String> getAccessibleFields(ReflectionContext context, String className) {
        // Don't expose fields — too many transitive deps
        return Collections.emptyList();
    }

    @Override
    public Collection<MethodDescriptor> getAccessibleMethods(ReflectionContext context, String className) {
        if (!INSTANTIABLE_CLASSES.contains(className)) {
            return Collections.emptyList();
        }
        // Only expose constructors (<init>) to avoid pulling in the full method graph
        var cls = context.getClassSource().get(className);
        if (cls == null) return Collections.emptyList();
        Set<MethodDescriptor> methods = new HashSet<>();
        for (var method : cls.getMethods()) {
            if (method.getName().equals("<init>")) {
                methods.add(method.getDescriptor());
            }
        }
        return methods;
    }
}
