# Packable Annotation Reflection Configuration Scripts

This document describes the scripts created to automatically update the TeaVM ReflectionConfig.java file with classes that use the `@Packable` annotation in the RemixedDungeon project.

## Overview

In the RemixedDungeon project, many classes use the `@Packable` annotation to mark fields or classes that need to be serialized. For the TeaVM build to work correctly, these classes need to be included in the reflection configuration. The scripts in this directory automate the process of identifying these classes and updating the ReflectionConfig.java file.

## Files

### 1. `update_packable_reflection_config.py`

This Python script updates the ReflectionConfig.java file with all classes that use the `@Packable` annotation.

**Features:**
- Contains a hardcoded list of 58 classes with `@Packable` annotations and their correct package names
- Updates ReflectionConfig.java with proper import statements
- Adds class references to the `enableReflectionForDebugging()` method
- Avoids duplicate imports and references

**Usage:**
```bash
python3 update_packable_reflection_config.py
```

### 2. `update_packable_reflection.sh`

This shell script provides a convenient way to run the Python script.

**Usage:**
```bash
./update_packable_reflection.sh
```

## How It Works

1. The Python script contains a predefined list of classes that use the `@Packable` annotation, with their correct package names.

2. When run, the script:
   - Reads the existing ReflectionConfig.java file
   - Adds import statements for all packable classes (avoiding duplicates)
   - Adds class references in the `enableReflectionForDebugging()` method
   - Writes the updated content back to the file

## Class List

The script handles the following classes with `@Packable` annotations:

```
com.nyrds.pixeldungeon.items.Carcass
com.nyrds.pixeldungeon.items.artifacts.SpellBook
com.nyrds.pixeldungeon.items.chaos.ChaosArmor
com.nyrds.pixeldungeon.items.chaos.ChaosBow
com.nyrds.pixeldungeon.items.chaos.ChaosMarkListener
com.nyrds.pixeldungeon.items.chaos.ChaosStaff
com.nyrds.pixeldungeon.items.chaos.ChaosSword
com.nyrds.pixeldungeon.items.common.GnollTamahawk
com.nyrds.pixeldungeon.items.necropolis.BlackSkull
com.nyrds.pixeldungeon.levels.RandomLevel
com.nyrds.pixeldungeon.levels.objects.Deco
com.nyrds.pixeldungeon.levels.objects.PortalGate
com.nyrds.pixeldungeon.levels.objects.PortalGateSender
com.nyrds.pixeldungeon.levels.objects.ScriptTrap
com.nyrds.pixeldungeon.levels.objects.Sign
com.nyrds.pixeldungeon.levels.objects.deprecatedSprite
com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor
com.nyrds.pixeldungeon.mechanics.buffs.CustomBuff
com.nyrds.pixeldungeon.mechanics.buffs.Moongrace
com.nyrds.pixeldungeon.mechanics.buffs.Necrotism
com.nyrds.pixeldungeon.mobs.common.Deathling
com.nyrds.pixeldungeon.mobs.common.ShadowLord
com.nyrds.pixeldungeon.mobs.guts.MimicAmulet
com.nyrds.pixeldungeon.mobs.guts.SuspiciousRat
com.nyrds.pixeldungeon.mobs.necropolis.Lich
com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC
com.nyrds.pixeldungeon.utils.ItemsList
com.nyrds.pixeldungeon.utils.Position
com.watabou.pixeldungeon.Record
com.watabou.pixeldungeon.actors.Char
com.watabou.pixeldungeon.actors.buffs.Buff
com.watabou.pixeldungeon.actors.buffs.Hunger
com.watabou.pixeldungeon.actors.buffs.Shadows
com.watabou.pixeldungeon.actors.buffs.burnItem
com.watabou.pixeldungeon.actors.mobs.Goo
com.watabou.pixeldungeon.actors.mobs.Mob
com.watabou.pixeldungeon.actors.mobs.Swarm
com.watabou.pixeldungeon.actors.mobs.Undead
com.watabou.pixeldungeon.actors.mobs.npcs.FetidRat
com.watabou.pixeldungeon.actors.mobs.npcs.Hedgehog
com.watabou.pixeldungeon.actors.mobs.npcs.MirrorImage
com.watabou.pixeldungeon.actors.mobs.npcs.RatKing
com.watabou.pixeldungeon.items.Codex
com.watabou.pixeldungeon.items.DewVial
com.watabou.pixeldungeon.items.armor.ClassArmor
com.watabou.pixeldungeon.items.armor.Glyph
com.watabou.pixeldungeon.items.armor.glyphs.DeferedDamage
com.watabou.pixeldungeon.items.keys.Key
com.watabou.pixeldungeon.items.quest.Pickaxe
com.watabou.pixeldungeon.items.wands.Wand
com.watabou.pixeldungeon.items.weapon.Enchantment
com.watabou.pixeldungeon.levels.BossLevel
com.watabou.pixeldungeon.levels.Level
com.watabou.pixeldungeon.logBookEntry
com.watabou.pixeldungeon.mechanics.ShadowCaster
com.watabou.pixeldungeon.plants.Armor
com.watabou.pixeldungeon.plants.Health
com.watabou.utils.Rect
```

## Maintenance

When new classes with `@Packable` annotations are added to the project:

1. Add the class name with its correct package to the `packable_classes` list in `update_packable_reflection_config.py`
2. Run the script to update ReflectionConfig.java:
   ```bash
   ./update_packable_reflection.sh
   ```

## Important Notes

- The ReflectionConfig.java file is critical for TeaVM builds to work correctly
- The `@Packable` annotation is used throughout the codebase for serialization
- These scripts ensure that all packable classes are properly included in the reflection configuration
- The scripts avoid creating duplicate imports or references