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
│  (pet inv UI) │    │  (new actions)  │   │  (integration) │
└───────────────┘    └─────────────────┘   └────────────────┘
        │
        ▼
┌───────────────┐    ┌─────────────────────────────┐
│ WndPetQuantity│    │  Shop-Style Interface       │
│ (quantity)    │    │  WndPetInventoryOptions     │
└───────────────┘    │  - Give to Pet              │
                     │  - Take from Pet            │
                     │  - View/Manage (full bag)   │
                     └─────────────────────────────┘
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
- Shows pet's equipped items + backpack (equipped items first)
- Tabs for pet's bags (if any)
- Action buttons: **Give**, **Take**, **Equip**, **Unequip** (via WndPetItem)
- Read-only for non-owner, editable for owner
- Title shows pet name and HP
- Uses `getListener()` getter instead of accessing private field
- **Overrides `updateItems()` to show equipped items on refresh**

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
- **Calls `bag.updateItems()` after equip/unequip to keep bag open**

### 5. WndPetInventoryOptions.java (New) - Shop-Style Interface
Main options window (like `WndShopOptions`):
- **Give to Pet** → Hero's backpack with `GiveToPetSelector`
- **Take from Pet** → Pet's backpack with `TakeFromPetSelector`
- **View/Manage** → Full `WndPetBag` (equipped items, unequip, etc.)

### 6. GiveToPetSelector / TakeFromPetSelector (New)
Listeners for bulk transfer (like `BuyItemSelector`/`SellItemSelector`):
- Handle single items directly
- Show `WndPetQuantity` for stackables
- Auto-refresh both hero and pet bag windows

### 7. New CommonActions
```java
MAC_PET_INVENTORY = "CharAction_PetInventory"
AC_GIVE_TO_PET = "PetInventory_ACGiveToPet"
AC_TAKE_FROM_PET = "PetInventory_ACTakeFromPet"
AC_EQUIP_ON_PET = "PetInventory_ACEquipOnPet"
AC_UNEQUIP_FROM_PET = "PetInventory_ACUnequipFromPet"
```
Note: Uses `MAC_` prefix for macro actions (consistent with MAC_ORDER, MAC_EXPEL), `AC_` for item actions.

### 8. Item Actions Integration
Modify `CharUtils.actions()` to include pet inventory action when:
- Target is a pet owned by hero (`target.getOwnerId() == hero.getId()`)

Add pet-specific actions to items based on context:
- In hero's inventory → "Give to Pet" (if not equipped)
- In pet's inventory → "Take from Pet", "Equip on Pet", "Unequip from Pet"

### 9. CharUtils.execute() Integration
Handle `MAC_PET_INVENTORY` action to open pet selection window.

### 10. Item._execute() Integration
Handle all pet action constants with proper type checks.

## Deviations from Original Plan

| Aspect | Original Plan | Actual Implementation |
|--------|---------------|----------------------|
| Pet Inventory Action | Added to OrderCellSelector (Move/Attack/Inventory) | Added to main pet interaction menu (Order/Expel/Inventory) via CharUtils |
| Action Constant Name | `AC_PET_INVENTORY` | `MAC_PET_INVENTORY` (consistent with MAC_ORDER, MAC_EXPEL) |
| equipItemOnPet() | No distance/ownership check | Added `Hero` parameter + `canAccessPetInventory()` validation |
| Lua/Modding | `@LuaInterface` on PetInventoryManager methods | ✅ All public methods annotated |
| Stackable partial transfer | Quantity selector UI | **Implemented**: WndPetQuantity with 1/5/10/50/100/500/1000/All |
| WndPetBag | "Read-only for non-owner" | Not implemented (single-player only) |
| **Inventory from pet menu** | Always shows WndPetSelect (even for single pet) | **Fixed**: Uses `target` pet directly, skips WndPetSelect |
| **Equippability indicators** | Not planned | **Added**: Slot icons (⚔️/🛡️/💍) with green/red coloring |
| **Equipped items display** | Not planned | **Added**: Shows equipped items in header area (weapon, armor, rings) with unequip action |
| **Equipment slots architecture** | `instanceof` checks in Belongings | **Polymorphic**: `Char.getAvailableEquipmentSlots()` overridden by Hero/Mob |
| **Pet inventory menu** | Direct to bag | **Shop-style**: Give/Take/ViewManage menu (WndPetInventoryOptions) |
| **Bulk transfer** | Per-item WndPetItem | **Shop-style**: WndPetInventoryOptions + selectors (GiveToPetSelector/TakeFromPetSelector) |
| **Unequip behavior** | Cancels if backpack full | **Aligned with hero**: Drops on ground if backpack full |
| **Bag refresh on action** | Closes bag | **Stays open**: updateItems() override refreshes equipped + backpack |

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
    if (target instanceof Mob && hero instanceof Hero) {
        Mob pet = (Mob) target;
        if (pet.getOwnerId() == hero.getId()) {
            PetInventoryManager.openPetInventoryOptions((Hero) hero, pet); // Opens menu
        }
    }
    return;
```

**Note**: Opens the shop-style menu with Give/Take/ViewManage options. WndPetSelect is only shown for "Give to Pet" from hero's inventory where no target pet is pre-selected.

### In Item.actions():
Context-aware pet actions based on item location and actor type.

### In Item._execute():
Full handling of all pet action constants with proper type checks.

## UI Flow

```
Hero taps pet → WndOptions [Move, Attack, Inventory, Expel]
                    ↓ (Inventory on THIS pet)
            MAC_PET_INVENTORY executes with `target` = that specific pet
                    ↓
            CharUtils.execute() uses `target` directly:
            PetInventoryManager.openPetInventoryOptions(hero, pet)  // Opens menu
                    ↓
            WndPetInventoryOptions (3 choices):
            ┌─────────────────────────────────────────┐
            │ PetName HP: X/Y                         │
            ├─────────────────────────────────────────┤
            │ 1. Give to Pet      → Hero's backpack   │
            │ 2. Take from Pet    → Pet's backpack    │
            │ 3. View/Manage      → WndPetBag (full)  │
            └─────────────────────────────────────────┘

--- Give/Take Flow (like shop) ---
Give to Pet:
    WndBag(hero's backpack) with GiveToPetSelector
        → Tap item → Stackable? → WndPetQuantity
        → Direct transfer → Auto-refresh both bags

Take from Pet:
    WndBag(pet's backpack) with TakeFromPetSelector
        → Tap item → Stackable? → WndPetQuantity
        → Direct transfer → Auto-refresh both bags

--- View/Manage Flow ---
View/Manage:
    WndPetBag (full pet inventory with equipped items)
        → Equipped items in header (weapon, armor, rings)
        → Tap equipped item → "Unequip from Pet"
        → Tap backpack item → "Equip on Pet" / "Take from Pet"
        → Auto-refresh on any action (updateItems() override)
```

## Equipment Slots Architecture (Final Design)

The equipment slot system uses clean polymorphism - each character type declares its own available slots:

```java
// Char.java (base - NPCs get no slots by default)
public Set<Belongings.Slot> getAvailableEquipmentSlots() {
    return Collections.emptySet();
}

// Hero.java (player gets all 5 slots)
@Override
public Set<Belongings.Slot> getAvailableEquipmentSlots() {
    return EnumSet.of(WEAPON, ARMOR, LEFT_HAND, ARTIFACT, LEFT_ARTIFACT);
}

// Mob.java (all pets - Deathling, etc. - inherit all slots)
@Override
public Set<Belongings.Slot> getAvailableEquipmentSlots() {
    return EnumSet.of(WEAPON, ARMOR, LEFT_HAND, ARTIFACT, LEFT_ARTIFACT);
}

// Belongings.java - delegates to owner, zero instanceof checks
private void configureAvailableSlots() {
    availableSlots.addAll(owner.getAvailableEquipmentSlots());
}
```

**Benefits:**
- Zero `instanceof` checks in Belongings
- New pet types automatically work (just extend Mob)
- New character types = override one method
- Deathling works automatically (extends Mob)
## UX Improvements Implemented

### 1. Quantity Selector for Stackable Items (`WndPetQuantity.java`)
- New window for stackable items (potions, scrolls, ammo, food)
- Quantity buttons: 1, 5, 10, 50, 100, 500, 1000 + "All"
- Works for both **Give to Pet** (hero → pet) and **Take from Pet** (pet → hero)
- Reuses pattern from `WndTradeItem` (shop buy/sell)
- Auto-refreshes parent `WndBag` on completion

### 2. Equippability Indicators in `WndPetBag`
- Shows slot icons in item slots' top-right corner:
  - ⚔️ Weapon
  - 🛡️ Armor
  - 💍 Rings/Artifacts (left hand, artifact slots)
- Color-coded:
  - **Green** = Pet can equip (meets STR requirements, slot available)
  - **Red ⚠** = Pet cannot equip (insufficient STR, slot blocked)
- Uses reflection to access private `topRight` BitmapText in `ItemSlot`
- Re-applies on tab switch via `onClick(Tab)` override

### 3. HP Title Update on Tab Switch
- Window title shows: "PetName HP: current/max"
- Updates when switching between bag tabs (backpack, potion belt, etc.)
- Uses reflection to access private `txtTitle` and `panelWidth` fields in `WndBag`
- Re-centers title after update

### 4. Direct Pet Inventory Access (Fixed)
- Clicking "Inventory" on a specific pet opens THAT pet's bag directly
- No more unnecessary `WndPetSelect` for single-pet owners
- `WndPetSelect` only shown for "Give to Pet" from hero's inventory

### 5. Equipped Items Display in Pet Inventory
- Equipped items now shown in header area (weapon, armor, left hand, 2 artifact slots)
- Uses `WndBag.placeEquipped()` (protected) for pet equipped items
- Equipped items are clickable and show "Unequip from Pet" action in `WndPetItem`
- Fixes issue where equipped items became inaccessible after equipping

### 6. Shop-Style Pet Inventory Interface (New)
- **WndPetInventoryOptions**: Main menu with 3 choices (Give/Take/ViewManage)
- **Give to Pet**: Hero's backpack with `GiveToPetSelector` (like BuyItemSelector)
- **Take from Pet**: Pet's backpack with `TakeFromPetSelector` (like SellItemSelector)
- **View/Manage**: Full `WndPetBag` with equipped items + unequip
- **WndPetQuantity**: Quantity picker for stackables (like WndTradeItem)
- Auto-refreshes both hero and pet bag windows on any action

### 7. Unequip Aligned with Hero Behavior
- If pet's backpack full → item drops on ground (matches hero's `doUnequip`)
- Previously cancelled unequip; now consistent with hero behavior

### 8. Bag Stays Open on Equip/Unequip
- `WndPetBag` overrides `updateItems()` to call `clearAndReplaceItems()`
- Shows equipped items + backpack after any action
- Equipped items appear first, then backpack items
- Matches hero inventory behavior exactly

1. **Serialization**: Pets already save/load belongings via `Char.storeInBundle()`/`restoreFromBundle()`
2. **Equipment Validation**: Check STR requirements, slot compatibility for pets
3. **Pet AI**: Equipped items affect pet stats (damage, DR, etc.) - handled by `Belongings` system
4. **Lua/Modding**: All PetInventoryManager methods exposed via `@LuaInterface`
5. **Cursed Items**: Cannot unequip from pet (same as hero)
6. **Distance Check**: Hero and pet must be adjacent or on same cell
7. **Save/Load Equipment Slots**: `Belongings.restoreFromBundle()` calls `configureAvailableSlots()` to re-configure slots after loading (since `availableSlots` is not serialized)
8. **Bag Refresh Pattern**: `WndPetBag.updateItems()` override ensures equipped items stay visible on refresh (parent's method didn't show them)
9. **Protected API**: `WndBag` exposes protected fields/methods (`nCols`, `nRows`, `titleBottom`, `stuff`, `clearItems`, `placeItem`, `placeEquipped`) for subclass customization

## Files Created/Modified

| File | Type | Purpose |
|------|------|---------|
| `PetInventoryManager.java` | New | Core logic |
| `WndPetBag.java` | New | Pet inventory UI (extends WndBag) |
| `WndPetSelect.java` | New | Pet selection (if multiple) |
| `WndPetItem.java` | New | Pet-specific item actions |
| `WndPetQuantity.java` | New | Quantity selector for stackables |
| `WndPetInventoryOptions.java` | New | Shop-style menu (Give/Take/ViewManage) |
| `GiveToPetSelector.java` | New | Listener for hero→pet transfers |
| `TakeFromPetSelector.java` | New | Listener for pet→hero transfers |
| `PetItemSlot.java` | New | Placeholder for equippability indicators |
| `CommonActions.java` | Modify | Add action constants |
| `CharUtils.java` | Modify | Add pet inventory action + execution |
| `Item.java` | Modify | Pet-specific actions in actions() and _execute() |
| `Char.java` | Modify | Added `getAvailableEquipmentSlots()` base method |
| `Hero.java` | Modify | Override `getAvailableEquipmentSlots()` for Hero |
| `Mob.java` | Modify | Override `getAvailableEquipmentSlots()` for pets |
| `Belongings.java` | Modify | Delegates slot config to `owner.getAvailableEquipmentSlots()` |
| `WndBag.java` | Modify | Added `placeEquippedForOwner()`, exposed protected fields/methods |

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