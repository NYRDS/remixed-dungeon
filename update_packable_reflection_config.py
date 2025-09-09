#!/usr/bin/env python3
"""
Script to update ReflectionConfig.java with classes that use @Packable annotation
"""

import re

# List of classes with @Packable annotations (with correct package names)
packable_classes = [
    "com.nyrds.pixeldungeon.items.Carcass",
    "com.nyrds.pixeldungeon.items.artifacts.SpellBook",
    "com.nyrds.pixeldungeon.items.chaos.ChaosArmor",
    "com.nyrds.pixeldungeon.items.chaos.ChaosBow",
    "com.nyrds.pixeldungeon.items.chaos.ChaosMarkListener",
    "com.nyrds.pixeldungeon.items.chaos.ChaosStaff",
    "com.nyrds.pixeldungeon.items.chaos.ChaosSword",
    "com.nyrds.pixeldungeon.items.common.GnollTamahawk",
    "com.nyrds.pixeldungeon.items.necropolis.BlackSkull",
    "com.nyrds.pixeldungeon.levels.RandomLevel",
    "com.nyrds.pixeldungeon.levels.objects.Deco",
    "com.nyrds.pixeldungeon.levels.objects.PortalGate",
    "com.nyrds.pixeldungeon.levels.objects.PortalGateSender",
    "com.nyrds.pixeldungeon.levels.objects.ScriptTrap",
    "com.nyrds.pixeldungeon.levels.objects.Sign",
    "com.nyrds.pixeldungeon.levels.objects.deprecatedSprite",
    "com.nyrds.pixeldungeon.mechanics.actors.ScriptedActor",
    "com.nyrds.pixeldungeon.mechanics.buffs.CustomBuff",
    "com.nyrds.pixeldungeon.mechanics.buffs.Moongrace",
    "com.nyrds.pixeldungeon.mechanics.buffs.Necrotism",
    "com.nyrds.pixeldungeon.mobs.common.Deathling",
    "com.nyrds.pixeldungeon.mobs.common.ShadowLord",
    "com.nyrds.pixeldungeon.mobs.guts.MimicAmulet",
    "com.nyrds.pixeldungeon.mobs.guts.SuspiciousRat",
    "com.nyrds.pixeldungeon.mobs.necropolis.Lich",
    "com.nyrds.pixeldungeon.mobs.npc.ServiceManNPC",
    "com.nyrds.pixeldungeon.utils.ItemsList",
    "com.nyrds.pixeldungeon.utils.Position",
    "com.watabou.pixeldungeon.Record",
    "com.watabou.pixeldungeon.actors.Char",
    "com.watabou.pixeldungeon.actors.buffs.Buff",
    "com.watabou.pixeldungeon.actors.buffs.Hunger",
    "com.watabou.pixeldungeon.actors.buffs.Shadows",
    "com.watabou.pixeldungeon.actors.buffs.burnItem",
    "com.watabou.pixeldungeon.actors.mobs.Goo",
    "com.watabou.pixeldungeon.actors.mobs.Mob",
    "com.watabou.pixeldungeon.actors.mobs.Swarm",
    "com.watabou.pixeldungeon.actors.mobs.Undead",
    "com.watabou.pixeldungeon.actors.mobs.npcs.FetidRat",
    "com.watabou.pixeldungeon.actors.mobs.npcs.Hedgehog",
    "com.watabou.pixeldungeon.actors.mobs.npcs.MirrorImage",
    "com.watabou.pixeldungeon.actors.mobs.npcs.RatKing",
    "com.watabou.pixeldungeon.items.Codex",
    "com.watabou.pixeldungeon.items.DewVial",
    "com.watabou.pixeldungeon.items.armor.ClassArmor",
    "com.watabou.pixeldungeon.items.armor.Glyph",
    "com.watabou.pixeldungeon.items.armor.glyphs.DeferedDamage",
    "com.watabou.pixeldungeon.items.keys.Key",
    "com.watabou.pixeldungeon.items.quest.Pickaxe",
    "com.watabou.pixeldungeon.items.wands.Wand",
    "com.watabou.pixeldungeon.items.weapon.Enchantment",
    "com.watabou.pixeldungeon.levels.BossLevel",
    "com.watabou.pixeldungeon.levels.Level",
    "com.watabou.pixeldungeon.logBookEntry",
    "com.watabou.pixeldungeon.mechanics.ShadowCaster",
    "com.watabou.pixeldungeon.plants.Armor",
    "com.watabou.pixeldungeon.plants.Health",
    "com.watabou.utils.Rect"
]

def update_reflection_config(reflection_config_path, packable_classes):
    """Update ReflectionConfig.java with packable classes"""
    # Read the existing ReflectionConfig.java file
    with open(reflection_config_path, 'r') as f:
        content = f.read()
    
    # Find the position to insert imports (before the class declaration)
    class_decl_match = re.search(r'public class ReflectionConfig\s*\{', content)
    if not class_decl_match:
        print("Could not find class declaration in ReflectionConfig.java")
        return False
    
    class_decl_pos = class_decl_match.start()
    
    # Generate import statements (avoid duplicates with existing imports)
    existing_imports = set()
    for match in re.finditer(r'import\s+([^\s;]+);', content):
        existing_imports.add(match.group(1))
    
    new_imports = []
    for cls in packable_classes:
        if cls not in existing_imports:
            new_imports.append(f"import {cls};")
    
    # Insert imports before the class declaration
    import_section = "\n" + "\n".join(sorted(new_imports)) + "\n\n" if new_imports else "\n"
    updated_content = content[:class_decl_pos] + import_section + content[class_decl_pos:]
    
    # Find the enableReflectionForDebugging method
    method_match = re.search(r'public static void enableReflectionForDebugging\(\)\s*\{[^}]*\}', updated_content, re.DOTALL)
    if not method_match:
        print("Could not find enableReflectionForDebugging method in ReflectionConfig.java")
        return False
    
    method_start = method_match.start()
    method_end = method_match.end()
    method_content = method_match.group(0)
    
    # Find existing class references in the method to avoid duplicates
    existing_references = set()
    for match in re.finditer(r'(\w+(?:\.\w+)*)\.class\.getName\(\)', method_content):
        existing_references.add(match.group(1))
    
    # Generate new class references
    new_references = []
    for cls in packable_classes:
        simple_name = cls.split(".")[-1]
        if simple_name not in existing_references:
            new_references.append(f"        {simple_name}.class.getName();")
    
    # Add the new references to the method (before the last })
    if new_references:
        last_brace = method_content.rfind('}')
        updated_method_content = method_content[:last_brace] + "\n" + "\n".join(sorted(new_references)) + "\n" + method_content[last_brace:]
        updated_content = updated_content[:method_start] + updated_method_content + updated_content[method_end:]
    
    # Write the updated content back to the file
    with open(reflection_config_path, 'w') as f:
        f.write(updated_content)
    
    return True

def main():
    reflection_config_path = "/home/mike/StudioProjects/remixed-dungeon/RemixedDungeonHtml/src/html/java/com/nyrds/teavm/reflection/ReflectionConfig.java"
    
    print(f"Found {len(packable_classes)} classes with @Packable annotations:")
    for cls in packable_classes:
        print(f"  {cls}")
    
    print("\nUpdating ReflectionConfig.java...")
    if update_reflection_config(reflection_config_path, packable_classes):
        print("Successfully updated ReflectionConfig.java")
    else:
        print("Failed to update ReflectionConfig.java")

if __name__ == "__main__":
    main()