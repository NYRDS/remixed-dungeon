# Pet Inventory Management Implementation Plan

## Current System Analysis

### Pet System
- Pets are `Mob` instances with `fraction == Fraction.HEROES`
- Created via `Mob.makePet(Mob pet, Char owner)` - sets owner ID and fraction
- Each pet has its own `Belongings` (backpack + equipment slots) via `Char.getBelongings()`
- Player interacts via **Order** (move/attack) and **Expel** (release) actions in `CharUtils.actions()`

### Inventory System
- `Belongings`: 18-slot backpack + 5 equipment slots (weapon, armor, left hand, 2 rings)
- `WndBag`: Main inventory UI with tabs for bags (potion belt, quiver, etc.)
- `ItemButton`/`WndItem`: Item interaction with actions (equip, unequip, drop, etc.)
- `CommonActions`: Defines action constants (AC_EQUIP, AC_UNEQUIP, etc.)

### Trade System (Reference)
- `WndShopOptions`: Main shop window with Buy/Sell options
- `BuyItemSelector`/`SellItemSelector`: Implement `WndBag.Listener`
- `WndTradeItem`: Handles buy/sell with quantity selection
- Uses `item.detach(backpack, quantity)` and `item.collect(target)`

## Implementation Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    PetInventoryManager                      │
│  - Static methods for pet inventory operations              │
│  - giveItemToPet(hero, pet, item)                           │
│  - takeItemFromPet(hero, pet, item)                         │
│  - equipItemOnPet(hero, pet, item, slot)                    │
│  - unequipItemFromPet(pet, item)                            │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐    ┌─────────────────┐   ┌────────────────┐
│  WndPetBag    │    │  Pet Actions    │   │ CharUtils      │
│  (new window) │    │  (new actions)  │   │  (integration) │
└───────────────┘    └─────────────────┘   └────────────────┘
```

## Key Components to Create/Modify

### 1. PetInventoryManager.java (New)
Centralized logic for pet inventory operations:
- Validation (distance, pet alive, item compatibility, ownership)
- Transfer logic between hero ↔ pet backpacks
- Equipment slot management for pets
- Proper owner updates and sprite refresh
- Lua/Modding support via `@LuaInterface` annotations

### 2. WndPetBag.java (New - extends WndBag)
Pet inventory UI window:
- Shows pet's equipped items + backpack
- Tabs for pet's bags (if any)
- Action buttons: **Give**, **Take**, **Equip**, **Unequip** (via WndPetItem)
- Read-only for non-owner, editable for owner
- Title shows pet name and HP
- Uses `getListener()` getter instead of accessing private field

### 3. WndPetSelect.java (New)
Pet selection UI when hero has multiple pets:
- List pets with names, HP, type
- Tap to select and open WndPetBag
- Optional item parameter: if provided, gives item directly to selected pet

### 4. WndPetItem.java (New)
Pet-specific item action window:
- Shows item info with pet-specific actions based on context:
  - Item in hero's inventory → "Give to Pet"
  - Item in pet's backpack → "Take from Pet", "Equip on Pet"
  - Item equipped on pet → "Unequip from Pet"
- Handles cursed items correctly (cannot unequip)

### 5. New CommonActions
```java
MAC_PET_INVENTORY = "CharAction_PetInventory"
AC_GIVE_TO_PET = "PetInventory_ACGiveToPet"
AC_TAKE_FROM_PET = "PetInventory_ACTakeFromPet"
AC_EQUIP_ON_PET = "PetInventory_ACEquipOnPet"
AC_UNEQUIP_FROM_PET = "PetInventory_ACUnequipFromPet"
```
Note: Uses `MAC_` prefix for macro actions (consistent with MAC_ORDER, MAC_EXPEL), `AC_` for item actions.

### 6. Item Actions Integration
Modify `CharUtils.actions()` to include pet inventory action when:
- Target is a pet owned by hero (`target.getOwnerId() == hero.getId()`)

Add pet-specific actions to items based on context:
- In hero's inventory → "Give to Pet" (if not equipped)
- In pet's inventory → "Take from Pet", "Equip on Pet", "Unequip from Pet"

### 7. CharUtils.execute() Integration
Handle `MAC_PET_INVENTORY` action to open pet selection window.

### 8. Item._execute() Integration
Handle all pet inventory action constants with proper type casting.

## Deviations from Original Plan

| Aspect | Original Plan | Actual Implementation |
|--------|---------------|----------------------|
| Pet Inventory Action | Added to OrderCellSelector (Move/Attack/Inventory) | Added to main pet interaction menu (Order/Expel/Inventory) via CharUtils |
| Action Constant Name | `AC_PET_INVENTORY` | `MAC_PET_INVENTORY` (consistent with MAC_ORDER, MAC_EXPEL) |
| equipItemOnPet() | No distance/ownership check | Added `Hero` parameter + `canAccessPetInventory()` validation |
| Lua/Modding | `@LuaInterface` on PetInventoryManager methods | ✅ All public methods annotated |
| Stackable partial transfer | Quantity selector UI | Not implemented (full stack transfer only) |
| WndPetBag | "Read-only for non-owner" | Not implemented (single-player only) |

## Integration Points

### In CharUtils.actions():
```java
if (target.getOwnerId() == hero.getId()) {
    actions.add(CommonActions.MAC_ORDER);
    actions.add(CommonActions.MAC_EXPEL);
    actions.add(CommonActions.MAC_PET_INVENTORY);
}
```

### In CharUtils.execute():
```java
case CommonActions.MAC_PET_INVENTORY:
    if (target instanceof Mob) {
        PetInventoryManager.openPetSelect((Hero) hero);
    }
    return;
```

### In Item.actions():
Context-aware pet actions based on item location and actor type.

### In Item._execute():
Full handling of all pet action constants with proper type checks.

## UI Flow

```
Hero taps pet → WndOptions [Move, Attack, Inventory, Expel]
                    ↓ (Inventory)
            WndPetSelect (if >1 pet) 
                    ↓
            WndPetBag (pet's inventory with tabs)
                    ↓
            Item tap → WndPetItem with pet actions:
            [Give to Pet, Take from Pet, Equip on Pet, Unequip from Pet]
```

## Technical Considerations

1. **Serialization**: Pets already save/load belongings via `Char.storeInBundle()`/`restoreFromBundle()`
2. **Item Ownership**: `item.setOwner(pet)` / `item.setOwner(hero)` during transfers
3. **Equipment Validation**: Check STR requirements, slot compatibility for pets
4. **Pet AI**: Equipped items affect pet stats (damage, DR, etc.) - handled by `Belongings` system
5. **Lua/Modding**: All PetInventoryManager methods exposed via `@LuaInterface`
6. **Cursed Items**: Cannot unequip from pet (same as hero)
7. **Distance Check**: Hero and pet must be adjacent or on same cell

## Files Created/Modified

| File | Type | Purpose |
|------|------|---------|
| `PetInventoryManager.java` | New | Core logic |
| `WndPetBag.java` | New | Pet inventory UI (extends WndBag) |
| `WndPetSelect.java` | New | Pet selection (if multiple) |
| `WndPetItem.java` | New | Pet-specific item actions |
| `CommonActions.java` | Modify | Add action constants |
| `CharUtils.java` | Modify | Add pet inventory action + execution |
| `Item.java` | Modify | Pet-specific actions in actions() and _execute() |

## Implementation Order

1. **PetInventoryManager** - Core transfer/equip logic
2. **CommonActions** - Action constants
3. **WndPetBag** - Main pet inventory window
4. **WndPetSelect** - Pet selection window
5. **WndPetItem** - Pet-specific item action window
6. **CharUtils.actions()/execute()** - Integration
7. **Item.actions()/_execute()** - Context-aware actions
8. **Testing** - Verify with various pet types and items

## Build Verification

All builds pass:
- `./gradlew compileJava` ✅
- `./gradlew :RemixedDungeonDesktop:build` ✅
- `./gradlew --settings-file settings.android.gradle :RemixedDungeon:assembleAndroidFdroidDebug` ✅