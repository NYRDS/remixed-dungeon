---
name: pet-inventory-management
description: Implementation pattern for adding pet inventory management (give/take/equip/unequip items to pets)
source: auto-skill
extracted_at: '2026-06-11T15:24:51.878Z'
---

# Pet Inventory Management Implementation Pattern

This skill documents the approach for implementing pet inventory management in Remixed Dungeon, allowing players to give, take, equip, and unequip items on their pets.

## Architecture Overview

The implementation follows the existing codebase patterns:
- **Centralized Manager**: `PetInventoryManager` for all pet inventory operations
- **UI Window**: `WndPetBag` extending `WndBag` for pet inventory display
- **Action Integration**: New `CommonActions` constants for pet-specific actions
- **Pet Selection**: `WndPetSelect` for choosing among multiple pets

## Key Design Principles

### 1. Leverage Existing Systems
- Pets already have `Belongings` (backpack + equipment slots) via `Char.getBelongings()`
- Serialization handled by existing `Char.storeInBundle()`/`restoreFromBundle()`
- Equipment logic reused from `Belongings.equip()`/`unequip()`/`setItemForSlot()`
- UI patterns from `WndBag`/`ItemButton`/`WndItem`

### 2. Action Flow
```
Hero taps pet → Order menu → [Move, Attack, Inventory, Expel]
                          ↓
                  WndPetSelect (if >1 pet)
                          ↓
                  WndPetBag (pet's inventory)
                          ↓
                  Item tap → WndItem with pet actions:
                  [Give to Pet, Take from Pet, Equip on Pet, Unequip from Pet]
```

### 3. Core Components

#### PetInventoryManager.java (New)
Static utility class with methods:
- `giveItemToPet(Hero hero, Mob pet, Item item)` - Transfer from hero to pet
- `takeItemFromPet(Hero hero, Mob pet, Item item)` - Transfer from pet to hero
- `equipItemOnPet(Mob pet, EquipableItem item, Belongings.Slot slot)` - Equip on pet
- `unequipItemFromPet(Mob pet, EquipableItem item)` - Unequip from pet
- `canEquipOnPet(Mob pet, EquipableItem item, Belongings.Slot slot)` - Validation
- `openPetInventory(Hero hero, Mob pet)` - Opens WndPetBag

#### WndPetBag.java (New - extends WndBag)
- Constructor takes `Mob pet` instead of `Belongings`
- Shows pet's equipped items + backpack
- Action buttons for pet-specific operations
- Read-only mode for non-owners (future multiplayer)

#### WndPetSelect.java (New)
- Lists all hero's pets with name, HP, entity kind
- Tap to select → opens WndPetBag for that pet

#### CommonActions.java (Extend)
```java
public static final String AC_PET_INVENTORY = "PetInventory_ACOpen";
public static final String AC_GIVE_TO_PET = "PetInventory_ACGiveToPet";
public static final String AC_TAKE_FROM_PET = "PetInventory_ACTakeFromPet";
public static final String AC_EQUIP_ON_PET = "PetInventory_ACEquipOnPet";
public static final String AC_UNEQUIP_FROM_PET = "PetInventory_ACUnequipFromPet";
```

#### CharUtils.java (Extend `actions()`)
```java
if (target.isPet() && target.getOwnerId() == hero.getId()) {
    actions.add(CommonActions.AC_PET_INVENTORY);
}
```

#### OrderCellSelector.java (Extend)
Add "Inventory" option alongside Move/Attack in the cell selection prompt.

### 4. Item Action Integration
In `Item.actions(Char hero)` or `EquipableItem.actions(Char hero)`:
- If item in hero's backpack and hero has pets → add "Give to Pet"
- If item in pet's inventory → add "Take from Pet", "Equip on Pet", "Unequip from Pet"
- Use `PetInventoryManager` for actual operations

### 5. Validation Rules
- **Distance**: Hero and pet must be adjacent or same cell
- **Pet State**: Pet must be alive and `isPet() == true`
- **Ownership**: `pet.getOwnerId() == hero.getId()`
- **Equipment**: Check STR requirements, slot compatibility for pet
- **Capacity**: Pet backpack has 18 slots (same as hero)

### 6. Item Ownership Transfer
```java
// Give to pet
item.detach(hero.getBelongings().backpack);
item.setOwner(pet);
pet.getBelongings().collect(item);

// Take from pet
item.detach(pet.getBelongings().backpack);
item.setOwner(hero);
hero.getBelongings().collect(item);
```

### 7. Equipment on Pets
Pets use same `Belongings.Slot` enum:
- WEAPON, LEFT_HAND, ARMOR, ARTIFACT, LEFT_ARTIFACT
- `Belongings.equip()` / `unequip()` work identically
- Pet stats (damage, DR) automatically updated via existing systems

### 8. Lua/Modding Support
Add `@LuaInterface` annotations to `PetInventoryManager` methods:
```java
@LuaInterface
public static void giveItemToPet(Hero hero, Mob pet, Item item) { ... }

@LuaInterface
public static void openPetInventory(Hero hero, Mob pet) { ... }
```

## Implementation Order

1. **PetInventoryManager** - Core logic, no UI dependencies
2. **CommonActions** - Action constants
3. **WndPetBag** - Pet inventory UI
4. **WndPetSelect** - Pet selection (if needed)
5. **CharUtils/OrderCellSelector** - Integration points
6. **Item/EquipableItem** - Pet-specific actions
7. **Testing** - Various pet types, items, edge cases

## Edge Cases Handled

- **Cursed Items**: Cannot unequip if cursed (same as hero)
- **Stackable Items**: Transfer full stack or partial
- **Bags**: Pets can have bags (potion belt, quiver, etc.)
- **Pet Death**: Items drop on death (existing `Mob.dropAll()`)
- **Level Transition**: Pets follow hero, belongings persist
- **Multiple Pets**: Selection window handles this

## Testing Checklist

- [ ] Give item from hero to pet backpack
- [ ] Take item from pet to hero backpack
- [ ] Equip weapon/armor/ring on pet
- [ ] Unequip item from pet
- [ ] Cursed item handling on pet
- [ ] Stackable item partial transfer
- [ ] Pet with bags (potion belt, etc.)
- [ ] Multiple pets - selection works
- [ ] Save/load preserves pet inventory
- [ ] Pet death drops items
- [ ] Level transition preserves items
- [ ] Mod/Lua API access works