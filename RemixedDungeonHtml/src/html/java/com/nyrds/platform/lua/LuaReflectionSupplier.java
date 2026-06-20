package com.nyrds.platform.lua;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.teavm.classlib.ReflectionContext;
import org.teavm.classlib.ReflectionSupplier;
import org.teavm.model.MethodDescriptor;

import com.nyrds.generated.LuaClassMap;
import com.nyrds.platform.lua.LuaConstructorMap;

/**
 * Tells TeaVM which classes need reflective access because they are accessed
 * dynamically from Lua scripts via luajava.bindClass() / newInstance().
 *
 * Uses the generated LuaClassMap (built by the annotation processor from
 * @LuaInterface annotations) for surgical method/field/constructor exposure.
 * Also uses the Python-generated LuaClassMap for comprehensive class coverage
 * from Lua script scanning. Maintains supplementary sets for classes not yet
 * covered by either source.
 */
public class LuaReflectionSupplier implements ReflectionSupplier {

    /**
     * Classes that need constructors exposed (newInstance) but aren't yet
     * annotated with @LuaInterface. Keep in sync with scripts/lib/commonClasses.lua.
     */
    private static final Set<String> SUPPLEMENTARY_INSTANTIABLE = new HashSet<>();

    /**
     * Classes that only need Class.forName() support (bindClass static calls)
     * but aren't yet annotated with @LuaInterface.
     */
    private static final Set<String> SUPPLEMENTARY_CLASSES = new HashSet<>();

    static {
        // Objects.Ui — instantiated from Lua via RPD.new()
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.windows.WndMessage");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.windows.WndStory");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.windows.WndQuest");
        SUPPLEMENTARY_INSTANTIABLE.add("com.nyrds.pixeldungeon.windows.WndShopOptions");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.windows.WndChooseWay");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.noosa.Image");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.ui.Banner");
        SUPPLEMENTARY_INSTANTIABLE.add("com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor");

        // Tweeners — instantiated from Lua
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.noosa.tweeners.PosTweener");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.noosa.tweeners.JumpTweener");

        // Effects/SFX — instantiated from Lua
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.CellEmitter");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.BlobEmitter");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.noosa.particles.Emitter");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.MagicMissile");
        SUPPLEMENTARY_INSTANTIABLE.add("com.nyrds.pixeldungeon.effects.DeathStroke");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.HighlightCell");

        // Particle classes — instantiated from Lua
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.FlameParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.SnowParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.ShaftParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.ShadowParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.DarknessParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.EarthParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.EnergyParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.FlowParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.LeafParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.PoisonParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.PurpleParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.SparkParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.WebParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.WindParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.WoolParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.BloodParticle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.effects.particles.ElmoParticle");

        // Misc — instantiated from Lua
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.items.wands.WandOfBlink");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.items.wands.WandOfTelekinesis");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.items.wands.WandOfFirebolt");

        // Additional classes instantiated from Lua (from script scan)
        SUPPLEMENTARY_INSTANTIABLE.add("com.nyrds.pixeldungeon.windows.LuaWndBagListener");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.windows.WndBag");
        SUPPLEMENTARY_INSTANTIABLE.add("com.nyrds.pixeldungeon.utils.Position");
        SUPPLEMENTARY_INSTANTIABLE.add("com.nyrds.pixeldungeon.windows.WndOptionsLua");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.utils.Bundle");
        SUPPLEMENTARY_INSTANTIABLE.add("com.watabou.pixeldungeon.sprites.Glowing");

        // Classes only accessed via bindClass (static methods) — no constructor needed
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.utils.GLog");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.platform.game.RemixedDungeon");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.game.GameLoop");
        SUPPLEMENTARY_CLASSES.add("com.watabou.utils.PathFinder");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.platform.audio.Sample");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.platform.audio.MusicManager");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.platform.util.StringsManager");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.CharUtils");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.game.ModQuirks");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.util.Util");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.windows.WndBag");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.items.Treasury");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Buff");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Roots");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Paralysis");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Vertigo");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Invisibility");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Levitation");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Hunger");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Poison");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Frost");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Light");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Cripple");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Charm");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Blessed");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.MindVision");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.mechanics.buffs.Necrotism");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.mechanics.buffs.RageBuff");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Terror");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Amok");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Awareness");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Barkskin");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Sleep");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Slow");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.buffs.Blindness");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Blob");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Fire");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Foliage");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.ConfusionGas");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.LiquidFlame");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.ParalyticGas");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Darkness");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Web");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.ToxicGas");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.MiasmaGas");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Regrowth");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.WaterOfHealth");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.WaterOfTransmutation");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.WaterOfAwareness");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.blobs.Freezing");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.mechanics.Ballistica");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.Challenges");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.scenes.GameScene");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.Dungeon");
        SUPPLEMENTARY_CLASSES.add("com.watabou.noosa.Camera");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.ai.MobAi");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.items.common.ItemFactory");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.mobs.common.MobFactory");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.effects.EffectsFactory");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.mechanics.spells.SpellFactory");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.Effects");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.Speck");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.SpellSprite");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.FlameParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.SnowParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.ShaftParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.ShadowParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.DarknessParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.EarthParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.EnergyParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.FlowParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.LeafParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.PoisonParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.PurpleParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.SparkParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.WebParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.WindParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.WoolParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.BloodParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.effects.particles.ElmoParticle");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.Badges");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.items.ItemUtils");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.DungeonTilemap");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.util.ModdingMode");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.lua.LuaUtils");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.ui.QuickSlot");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.platform.app.Input");
        SUPPLEMENTARY_CLASSES.add("com.watabou.utils.SystemTime");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.levels.Terrain");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.Actor");
        SUPPLEMENTARY_CLASSES.add("com.nyrds.pixeldungeon.alchemy.AlchemyRecipes");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.Journal");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.levels.features.Chasm");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.actors.mobs.Mob");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.items.Heap");
        SUPPLEMENTARY_CLASSES.add("com.watabou.pixeldungeon.ui.BuffIndicator");
    }

    @Override
    public boolean isClassFoundByName(ReflectionContext context, String name) {
        // Check annotation processor's map (@LuaInterface annotated classes)
        if (LuaClassMap.ALL_CLASSES.contains(name)) {
            return true;
        }
        // Check Python script's map (classes found in Lua scripts)
        if (com.nyrds.platform.lua.LuaClassMap.contains(name)) {
            return true;
        }
        // Check supplementary sets
        return SUPPLEMENTARY_INSTANTIABLE.contains(name)
                || SUPPLEMENTARY_CLASSES.contains(name);
    }

    @Override
    public Collection<String> getAccessibleFields(ReflectionContext context, String className) {
        Set<String> fields = LuaClassMap.FIELDS.get(className);
        if (fields != null) {
            return fields;
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<MethodDescriptor> getAccessibleMethods(ReflectionContext context, String className) {
        var cls = context.getClassSource().get(className);
        if (cls == null) return Collections.emptyList();

        Set<MethodDescriptor> methods = new HashSet<>();

        // Expose @LuaInterface-annotated methods from generated map
        Set<String> methodNames = LuaClassMap.METHODS.get(className);
        if (methodNames != null) {
            for (var method : cls.getMethods()) {
                if (methodNames.contains(method.getName())) {
                    methods.add(method.getDescriptor());
                }
            }
        }

        // Expose constructors for classes in generated HAS_CONSTRUCTORS
        // and for supplementary instantiable classes
        boolean needsConstructor = LuaClassMap.HAS_CONSTRUCTORS.contains(className)
                || SUPPLEMENTARY_INSTANTIABLE.contains(className);

        if (needsConstructor) {
            for (var method : cls.getMethods()) {
                if (method.getName().equals("<init>")) {
                    methods.add(method.getDescriptor());
                }
            }
        }

        return methods;
    }
}
