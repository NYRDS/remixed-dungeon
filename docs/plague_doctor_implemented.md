# Plague Doctor Class - Implemented Abilities

## Overview
The Plague Doctor (internally known as "Doctor") class now has all its described abilities fully implemented.

## Implemented Abilities

### 1. Accuracy Penalty Without Bone Saw
- **Status**: ✅ Already Implemented
- **Location**: `HeroClass.java` in the `attackSkillBonus()` method
- **Mechanic**: When the Doctor class is not wielding the Bone Saw in either main or secondary weapon slot, there's a -5 penalty to attack skill

### 2. Starting Equipment
- **Status**: ✅ Implemented (in previous update)
- **Equipment**: Bone Saw (weapon) + Plague Doctor Mask (accessory)
- **Location**: `initHeroes.json` and `initHeroesDebug.json`

### 3. Critical Hits Drop Extra Parts
- **Status**: ✅ Now Implemented
- **Location**: `Char.java` - `checkCriticalHit()` and `dropExtraParts()` methods
- **Mechanic**: When the Doctor scores a critical hit (attack roll significantly exceeds defense roll), they drop extra harvestable parts (Toxic Glands, Rotten Organs, Bone Shards, or Vile Essence)

### 4. Bone Saw Special Mechanics
- **Status**: ✅ Now Implemented
- **Location**: `BoneSaw.lua` - modified `attackProc` function
- **Mechanic**: The Bone Saw deals +50% damage to paralyzed foes when wielded by the Doctor class

### 5. Toxic Gas Healing Mechanic
- **Status**: ✅ Now Implemented
- **Location**: `ToxicGas.java` - modified `poison()` method
- **Mechanic**: When an enemy dies from toxic gas damage, the Doctor hero is healed for 1 HP

### 6. Ranged Weapon Restriction
- **Status**: ✅ Now Implemented
- **Location**: `Char.java` - modified `shoot()` method
- **Mechanic**: The Doctor class cannot use ranged weapons (missile weapons), with an appropriate message displayed when attempted

## Summary
All six abilities described in the Plague Doctor perk text are now fully implemented:
- ✅ Accuracy penalty without Bone Saw
- ✅ Starting equipment (Bone Saw + Plague Doctor Mask)
- ✅ Critical hits drop extra parts
- ✅ Bone Saw deals +50% damage to paralyzed foes
- ✅ Enemies killed by Toxic Gas heal the Doctor for 1 HP per enemy
- ✅ Cannot use ranged weapons

The Plague Doctor class is now fully implemented as described in the perk text.