# Sprite Migration Analysis

Analysis of which Java sprite classes can be migrated using only the extras system.

## Sprites That Can Use Simple Extras Migration

### 1. GooSprite
- Custom animation: `pump` (called via `pumpUp()` method)
- Custom blood color: `0xFF000000`
- No other complex behaviors
- ✅ Can migrate with extras
- ✅ MIGRATED

### 2. MonkSprite
- Custom animation: `kick` (played randomly during attacks)
- No other complex behaviors
- ✅ Can migrate with extras
- ✅ MIGRATED (with 50% probability)

### 3. SeniorSprite
- Custom animation: `kick` (played randomly during attacks)
- No other complex behaviors
- ✅ Can migrate with extras
- ✅ MIGRATED (with 30% probability)

### 4. RatSprite
- Only standard animations, no extras needed
- ✅ Can migrate (already done as example)
- ✅ MIGRATED

### 5. SheepSprite
- Only standard animations, no extras needed
- ✅ Can migrate
- ✅ MIGRATED

### 6. LarvaSprite
- Custom blood color: `0xbbcc66`
- Splash effect on death
- ✅ Can migrate with death effects system
- ✅ MIGRATED

### 7. DM300Sprite
- Custom blood color: `0xFFFFFF88`
- Particle effect on death (wool particles)
- ✅ Can migrate with death effects system
- ✅ MIGRATED

### 8. YogSprite
- Splash effect on death
- Custom blood color: `0xFF0000FF`
- ✅ Can migrate with death effects system
- ✅ MIGRATED

### 9. BlacksmithSprite
- Particle emitter (forge particles)
- Sound effects
- ✅ Can migrate with particle emitters system
- ✅ MIGRATED

### 10. GhostSprite
- Custom drawing (blending mode)
- Particle effects on death
- Custom blood color: `0xFFFFFF`
- ❌ Has complex behaviors (drawing, particles)

### 11. ImpSprite
- Custom alpha transparency
- Particle effects on death
- ❌ Has complex behaviors (alpha, particles)

### 12. PiranhaSprite
- Water ripple effect on attack
- ❌ Has complex behavior (ripple effect)

### 13. RottingFistSprite
- Physics-based attack animation
- Camera shake effect
- ❌ Has complex behaviors (physics, camera)

### 14. ShopkeeperSprite
- Particle emitter (coin particles)
- ❌ Has complex behavior (emitter)

## Sprites Suitable for Simple Migration

1. **GooSprite** - Needs `pump` extra animation
2. **MonkSprite** - Needs `kick` extra animation
3. **SeniorSprite** - Needs `kick` extra animation
4. **RatSprite** - Already standard animations only
5. **SheepSprite** - Already standard animations only

## Sprites Successfully Migrated

1. **GooSprite** - JSON definition created, mob updated to use playExtra
2. **MonkSprite** - JSON definition created with proper callback handling
3. **SeniorSprite** - JSON definition created with proper callback handling
4. **SheepSprite** - JSON definition created
5. **LarvaSprite** - JSON definition created with death effects
6. **DM300Sprite** - JSON definition created with death effects
7. **YogSprite** - JSON definition created with death effects
8. **BlacksmithSprite** - JSON definition created with particle emitters

## Recent Improvements

- **Callback Support**: Added `playExtra(String key, Callback callback)` method to CharSprite to support animation completion callbacks
- **Death Effects System**: Added support for particle effects and splash effects on death
- **Particle Emitters System**: Added support for creating and managing persistent particle emitters
- **Proper Animation Flow**: Kick animations now properly trigger `onAttackComplete()` when they finish
- **Backward Compatibility**: Existing code using `playExtra(key)` continues to work unchanged