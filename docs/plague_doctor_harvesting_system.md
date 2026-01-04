# Plague Doctor Harvesting System Implementation

## Overview
This document tracks the implementation of the Plague Doctor harvesting system, which allows the Doctor class to harvest special materials from defeated enemies that can be used in alchemical processes.

## Implemented Harvest Items
The following harvest items have been implemented as Lua scripts:

1. **Toxic Gland** (`ToxicGland.lua`)
   - Sprite: Index 3 in materials.png
   - Description: A toxic gland harvested from a defeated enemy
   - Price: 5 gold
   - Stackable: Yes

2. **Rotten Organ** (`RottenOrgan.lua`)
   - Sprite: Index 4 in materials.png
   - Description: A decomposing organ harvested from a defeated enemy
   - Price: 5 gold
   - Stackable: Yes

3. **Bone Shard** (`BoneShard.lua`)
   - Sprite: Index 5 in materials.png
   - Description: A sharp shard of bone harvested from a defeated enemy
   - Price: 5 gold
   - Stackable: Yes

4. **Vile Essence** (`VileEssence.lua`)
   - Sprite: Index 6 in materials.png
   - Description: A concentrated essence of corruption harvested from a defeated enemy
   - Price: 10 gold
   - Stackable: Yes

## Implementation Status
- ✅ Harvest items created as Lua scripts
- ✅ Items use materials.png sprites (indices 3-6)
- ✅ Items are stackable and have appropriate descriptions
- ✅ Critical hit harvesting mechanism in Char.java should now work properly

## Next Steps
1. Test that the harvesting system works properly with the Doctor class
2. Implement alchemy recipes that use these materials
3. Create a blood collection system as mentioned in the Plague Doctor perk
4. Implement Plague Doctor-specific alchemy mechanics

## Files Created
- `/RemixedDungeon/src/main/assets/scripts/items/ToxicGland.lua`
- `/RemixedDungeon/src/main/assets/scripts/items/RottenOrgan.lua`
- `/RemixedDungeon/src/main/assets/scripts/items/BoneShard.lua`
- `/RemixedDungeon/src/main/assets/scripts/items/VileEssence.lua`