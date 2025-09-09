package com.nyrds.teavm.reflection;

import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.actors.Actor;
import org.teavm.interop.Platforms; // Example class from TeaVM interop

import com.nyrds.pixeldungeon.items.Carcass;
import com.nyrds.pixeldungeon.items.artifacts.SpellBook;
import com.nyrds.pixeldungeon.items.chaos.ChaosArmor;
import com.nyrds.pixeldungeon.items.chaos.ChaosBow;
import com.nyrds.pixeldungeon.items.chaos.ChaosMarkListener;
import com.nyrds.pixeldungeon.items.chaos.ChaosStaff;
import com.nyrds.pixeldungeon.items.chaos.ChaosSword;
import com.nyrds.pixeldungeon.items.common.GnollTamahawk;
import com.nyrds.pixeldungeon.items.necropolis.BlackSkull;
import com.nyrds.pixeldungeon.levels.RandomLevel;
import com.nyrds.pixeldungeon.levels.objects.Deco;
import com.nyrds.pixeldungeon.levels.objects.PortalGate;
import com.nyrds.pixeldungeon.levels.objects.PortalGateSender;
import com.nyrds.pixeldungeon.levels.objects.ScriptTrap;
import com.nyrds.pixeldungeon.levels.objects.Sign;
import com.nyrds.pixeldungeon.levels.objects.deprecatedSprite;
import com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor;
import com.nyrds.pixeldungeon.mechanics.buffs.CustomBuff;
import com.nyrds.pixeldungeon.mechanics.buffs.Moongrace;
import com.nyrds.pixeldungeon.mechanics.buffs.Necrotism;
import com.nyrds.pixeldungeon.mobs.common.Deathling;
import com.nyrds.pixeldungeon.mobs.common.ShadowLord;
import com.nyrds.pixeldungeon.mobs.guts.MimicAmulet;
import com.nyrds.pixeldungeon.mobs.guts.SuspiciousRat;
import com.nyrds.pixeldungeon.mobs.necropolis.Lich;
import com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC;
import com.nyrds.pixeldungeon.utils.ItemsList;
import com.nyrds.pixeldungeon.utils.Position;
import com.watabou.pixeldungeon.Record;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Hunger;
import com.watabou.pixeldungeon.actors.buffs.Shadows;
import com.watabou.pixeldungeon.actors.buffs.burnItem;
import com.watabou.pixeldungeon.actors.mobs.Goo;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Swarm;
import com.watabou.pixeldungeon.actors.mobs.Undead;
import com.watabou.pixeldungeon.actors.mobs.npcs.FetidRat;
import com.watabou.pixeldungeon.actors.mobs.npcs.Hedgehog;
import com.watabou.pixeldungeon.actors.mobs.npcs.MirrorImage;
import com.watabou.pixeldungeon.actors.mobs.npcs.RatKing;
import com.watabou.pixeldungeon.items.Codex;
import com.watabou.pixeldungeon.items.DewVial;
import com.watabou.pixeldungeon.items.armor.ClassArmor;
import com.watabou.pixeldungeon.items.armor.Glyph;
import com.watabou.pixeldungeon.items.armor.glyphs.DeferedDamage;
import com.watabou.pixeldungeon.items.keys.Key;
import com.watabou.pixeldungeon.items.quest.Pickaxe;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.weapon.Enchantment;
import com.watabou.pixeldungeon.levels.BossLevel;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.logBookEntry;
import com.watabou.pixeldungeon.mechanics.ShadowCaster;
import com.watabou.pixeldungeon.plants.Armor;
import com.watabou.pixeldungeon.plants.Health;
import com.watabou.utils.Rect;


public class ReflectionConfig {
    /**
     * This method is a powerful debugging tool that references classes to prevent
     * TeaVM from removing them during optimization.
     *
     * WARNING: DO NOT USE THIS IN PRODUCTION. It will dramatically increase the size
     * of your JavaScript file. Use this only to identify the exact classes that
     * need reflection, then annotate them with @Reflectable individually.
     */
    public static void enableReflectionForDebugging() {
        System.out.println("Enabling reflection for debugging purposes.");
        
        // Reference key classes that might be causing issues
        Hero.class.getName();
        CharSprite.class.getName();
        Actor.class.getName();
        Platforms.class.getName();
        
        // Also reference some common Java classes
        String.class.getName();
        Object.class.getName();
        Integer.class.getName();
        Boolean.class.getName();
        Float.class.getName();
        
        // Reference classes with @Packable annotation
        Carcass.class.getName();
        SpellBook.class.getName();
        ChaosArmor.class.getName();
        ChaosBow.class.getName();
        ChaosMarkListener.class.getName();
        ChaosStaff.class.getName();
        ChaosSword.class.getName();
        GnollTamahawk.class.getName();
        BlackSkull.class.getName();
        RandomLevel.class.getName();
        Deco.class.getName();
        PortalGate.class.getName();
        PortalGateSender.class.getName();
        ScriptTrap.class.getName();
        Sign.class.getName();
        deprecatedSprite.class.getName();
        ScriptedActor.class.getName();
        CustomBuff.class.getName();
        Moongrace.class.getName();
        Necrotism.class.getName();
        Deathling.class.getName();
        ShadowLord.class.getName();
        MimicAmulet.class.getName();
        SuspiciousRat.class.getName();
        Lich.class.getName();
        ServiceManNPC.class.getName();
        ItemsList.class.getName();
        Position.class.getName();
        Record.class.getName();
        Char.class.getName();
        Buff.class.getName();
        Hunger.class.getName();
        Shadows.class.getName();
        burnItem.class.getName();
        Goo.class.getName();
        Mob.class.getName();
        Swarm.class.getName();
        Undead.class.getName();
        FetidRat.class.getName();
        Hedgehog.class.getName();
        MirrorImage.class.getName();
        RatKing.class.getName();
        Codex.class.getName();
        DewVial.class.getName();
        ClassArmor.class.getName();
        Glyph.class.getName();
        DeferedDamage.class.getName();
        Key.class.getName();
        Pickaxe.class.getName();
        Wand.class.getName();
        Enchantment.class.getName();
        BossLevel.class.getName();
        Level.class.getName();
        logBookEntry.class.getName();
        ShadowCaster.class.getName();
        Armor.class.getName();
        Health.class.getName();
        Rect.class.getName();
    }
}