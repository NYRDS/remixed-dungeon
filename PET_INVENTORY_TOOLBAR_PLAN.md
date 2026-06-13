# Pet Inventory Toolbar Button Implementation Plan

## Overview
Add a dedicated pet inventory button to the game toolbar (bottom UI) that appears when the hero has pets, providing quick access to pet inventory management without needing to tap the pet first.

## Current Toolbar Structure (Toolbar.java)

```
[Action Buttons] [Quickslots] [Inventory/Spells]
Left side:        Wait(7)  Search(8)  Info(9)
Right side:       [Spells if caster]  Inventory(10)
```

## Implementation Plan

### 1. PetInventoryManager - Add Helper Method
Add a static method to open pet inventory directly from toolbar context:
```java
public static void openPetInventoryFromToolbar(@NotNull Hero hero) {
    List<Mob> pets = getHeroPets(hero);
    if (pets.isEmpty()) return;
    
    if (pets.size() == 1) {
        // Single pet - open inventory options directly
        openPetInventoryOptions(hero, pets.get(0));
    } else {
        // Multiple pets - show pet selector
        openPetSelect(hero);
    }
}
```

### 2. Toolbar.java - Add Pet Inventory Button
Add a new button `btnPetInventory`:
- Only visible when `PetInventoryManager.hasPets(hero)`
- Icon: Need to find unused index in UI_ICONS_12 (suggest index 13 or 14 if free)
- Position: Between Info(9) and Spells/Inventory
- onClick: `PetInventoryManager.openPetInventoryFromToolbar(hero)`

### 3. Icon Selection
Current UI_ICONS_12 indices in use:
- 6: Default spell / Elemental
- 7: Wait
- 8: Search  
- 9: Info
- 10: Inventory
- 11: Necromancy
- 12: Rage
- 13: ?
- 14: ?
- 15: Combat
- 16: Rogue
- 17: Witchcraft
- 18: Huntress
- 19: Elf

Need to verify 13, 14 are free. If not, may need to add new icon to ui_icons12x12.png.

### 4. Toolbar Layout Changes
Modify `layout()` method to:
- Conditionally add `btnPetInventory` to actionBox when hero has pets
- Update layout logic to accommodate extra button
- Handle handedness (left/right alignment)

### 5. Dynamic Visibility
Button should appear/disappear when:
- Pet gained (summoned, charmed)
- Pet lost (killed, expelled)
- Could observe via `PetInventoryManager.hasPets()` check in layout()

## Files to Modify

| File | Changes |
|------|---------|
| `PetInventoryManager.java` | Add `openPetInventoryFromToolbar(Hero)` method |
| `Toolbar.java` | Add `btnPetInventory` field, instantiate in constructor, add to layout() conditionally |
| `Chrome.java` / Assets | Verify icon index 13/14 available or add new icon |

## UX Considerations

1. **Button Position**: After Info (9), before Spells/Inventory - grouped with action buttons
2. **Tooltip/Long-press**: Could show pet count on long-press
3. **Disabled State**: Gray out when no pets (or hide entirely - hiding is cleaner)
4. **Multiple Pets**: Opens WndPetSelect to choose which pet's inventory

## Alternative: Quickslot-style Pet Button
Could also add as a quickslot that shows pet portrait, but toolbar button is simpler and matches existing pattern.

## Testing Checklist
- [ ] Button appears when hero has 1 pet
- [ ] Button appears when hero has multiple pets  
- [ ] Button hidden when no pets
- [ ] Single pet → opens WndPetInventoryOptions directly
- [ ] Multiple pets → opens WndPetSelect
- [ ] Works with both handedness settings
- [ ] Layout doesn't break on narrow screens
- [ ] All 3 builds pass