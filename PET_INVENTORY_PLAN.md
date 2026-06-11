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
│  - equipItemOnPet(pet, item, slot)                          │
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
- Validation (distance, pet alive, item compatibility)
- Transfer logic between hero ↔ pet backpacks
- Equipment slot management for pets
- Proper owner updates and sprite refresh

### 2. WndPetBag.java (New - extends WndBag)
Pet inventory UI window:
- Shows pet's equipped items + backpack
- Tabs for pet's bags (if any)
- Action buttons: **Give**, **Take**, **Equip**, **Unequip**
- Read-only for non-owner, editable for owner

### 3. WndPetSelect.java (New)
Pet selection UI when hero has multiple pets:
- List pets with names, HP, type
- Tap to select and open WndPetBag

### 4. New CommonActions
```java
AC_PET_INVENTORY = "PetInventory_ACPetInventory"
AC_GIVE_TO_PET = "PetInventory_ACGiveToPet"
AC_TAKE_FROM_PET = "PetInventory_ACTakeFromPet"
AC_EQUIP_ON_PET = "PetInventory_ACEquipOnPet"
AC_UNEQUIP_FROM_PET = "PetInventory_ACUnequipFromPet"
```

### 5. Item Actions Integration
Modify `CharUtils.actions()` to include pet inventory action when:
- Target is a pet owned by hero (`target.getOwnerId() == hero.getId()`)

Add pet-specific actions to items based on context:
- In hero's inventory → "Give to Pet"
- In pet's inventory → "Take from Pet", "Equip on Pet", "Unequip from Pet"

### 6. OrderCellSelector Modification
Add "Inventory" option alongside Move/Attack in pet order menu.

## Integration Points

### In CharUtils.actions():
```java
if (target.isPet() && target.getOwnerId() == hero.getId()) {
    actions.add(CommonActions.AC_PET_INVENTORY);
}
```

### In OrderCellSelector.onSelect():
Add case for inventory action to open WndPetSelect or WndPetBag directly.

## UI Flow

```
Hero taps pet → WndOptions [Move, Attack, Inventory, Expel]
                    ↓ (Inventory)
            WndPetSelect (if >1 pet) 
                    ↓
            WndPetBag (pet's inventory with tabs)
                    ↓
            Item tap → WndItem with pet actions:
            [Give to Pet, Take from Pet, Equip on Pet, Unequip from Pet]
```

## Technical Considerations

1. **Serialization**: Pets already save/load belongings via `Char.storeInBundle()`/`restoreFromBundle()`
2. **Item Ownership**: `item.setOwner(pet)` / `item.setOwner(hero)` during transfers
3. **Equipment Validation**: Check STR requirements, slot compatibility for pets
4. **Pet AI**: Equipped items should affect pet stats (damage, DR, etc.) - already handled by `Belongings` system
5. **Lua/Modding**: Expose pet inventory methods via `@LuaInterface` for mod support

## Files to Create/Modify

| File | Type | Purpose |
|------|------|---------|
| `PetInventoryManager.java` | New | Core logic |
| `WndPetBag.java` | New | Pet inventory UI |
| `WndPetSelect.java` | New | Pet selection (if multiple) |
| `CommonActions.java` | Modify | Add action constants |
| `CharUtils.java` | Modify | Add pet inventory action |
| `OrderCellSelector.java` | Modify | Add "Inventory" option |
| `Item.java`/`EquipableItem.java` | Modify | Pet-specific actions |

## Implementation Order

1. **PetInventoryManager** - Core transfer/equip logic
2. **CommonActions** - Action constants
3. **WndPetBag** - Main pet inventory window
4. **WndPetSelect** - Pet selection window
5. **CharUtils.actions()** - Integration
6. **OrderCellSelector** - Menu integration
6. **Item/EquipableItem actions()** - Context-aware actions
7. **Testing** - Verify with various pet types and items